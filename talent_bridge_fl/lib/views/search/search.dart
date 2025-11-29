import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/components/user_pfp_cached.dart';
import 'package:talent_bridge_fl/views//user-profile/user_profile.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:talent_bridge_fl/analytics/analytics_timer.dart';
import 'package:talent_bridge_fl/services/search_local_cache.dart';
import 'package:talent_bridge_fl/views/search/search_analytics_debug.dart';
import 'package:firebase_analytics/firebase_analytics.dart';

// ---- Tokens ----
const kBg = Color(0xFFFEF7E6); // cream
const kAmber = Color(0xFFFFC107); // amber accents
const kBrandGreen = Color(0xFF568C73); // brand green (logo fallback)
const kShadowCol = Color(0x33000000); // 20% black shadow
const kPillRadius = 26.0; // pill look

class Search extends StatefulWidget {
  const Search({super.key});

  @override
  State<Search> createState() => _SearchState();
}

class _SearchState extends State<Search> {
  // Search state
  final _queryCtrl = TextEditingController();
  final _firebaseService = FirebaseService();
  final FirebaseAnalytics _analytics = FirebaseAnalytics.instance;
  Timer? _debounce;

  // ---- BQ: medir time-to-first-content de People ----
  late final ScreenTimer _tPeople;
  bool _ttfcSent = false; // evita duplicar el evento

  // User data
  List<UserEntity> _allUsers = [];
  UserEntity? _currentUser;

  // Search results
  List<UserEntity> _searchResults = [];
  Map<UserEntity, double> _userScores = {};

  // Resultados cacheados (uid + displayName)
  List<SearchUserSummary> _cachedResults = [];

  // Fake “recent” items for the UI
  final _recents = const <String>[
    'Daniel Triviño',
    'ROBOCOL',
    'Proyectos Inteligencia Artificial',
  ];

  @override
  void initState() {
    super.initState();
    // Inicia cronómetro para People (Search)
    _tPeople = ScreenTimer(
      'first_content_people',
      baseParams: {'screen': 'People'},
    );

    _loadUserData();
    _loadCachedSearchResults();
    // Listen for search bar changes
    _queryCtrl.addListener(_onQueryChanged);
    WidgetsBinding.instance.addPostFrameCallback((_) {});
  }

  Future<void> _loadUserData() async {
    // Get all users
    final users = await _firebaseService.getAllUsers();
    setState(() {
      _allUsers = users;
      final uid = _firebaseService.currentUid();
      if (uid != null) {
        for (final u in _allUsers) {
          if (u.id == uid) {
            _currentUser = u;
            break;
          }
        }
      }
      // Initial search results empty
      _searchResults = [];
    });
  }

  Future<void> _loadCachedSearchResults() async {
    final cached = await SearchLocalCache.getLastUserResults();
    if (!mounted) return;
    setState(() {
      _cachedResults = cached;
    });
  }

  @override
  void dispose() {
    _queryCtrl.removeListener(_onQueryChanged);
    _queryCtrl.dispose();
    super.dispose();
  }

  void _onQueryChanged() {
    final q = _queryCtrl.text.trim().toLowerCase();
    if (q.isEmpty) {
      setState(() {
        _searchResults = [];
        _userScores = {};
      });
      return;
    }

    _runSearch(q);
  }

  void _runSearch(String q) {
    final projects = _currentUser?.projects ?? [];
    final skills = projects
        .expand((i) => i.skills)
        .map((j) => j.toLowerCase())
        .toList();

    final frequencies = skills.fold<Map<String, int>>(
      {},
      (map, item) => map..update(item, (v) => v + 1, ifAbsent: () => 1),
    );

    final weights = frequencies.map(
      (s, i) => MapEntry(s, i / (skills.isEmpty ? 1 : skills.length)),
    );

    final filteredUsers = _allUsers
        .where((u) => u.displayName.toLowerCase().contains(q))
        .toList();

    if (!_ttfcSent && filteredUsers.isNotEmpty) {
      _ttfcSent = true;
      _tPeople.endOnce(source: 'cache', itemCount: filteredUsers.length);
    }

    final Map<UserEntity, double> scores = {};
    for (var u in filteredUsers) {
      double score = 0;
      var uSkills = (u.skillsOrTopics ?? []).map((i) => i.toLowerCase());
      for (var skill in uSkills) {
        score += weights[skill] ?? 0;
      }
      scores[u] = score;
    }

    filteredUsers.sort((a, b) => -scores[a]!.compareTo(scores[b]!));

    setState(() {
      _userScores = scores;
      _searchResults = filteredUsers;
    });

    // Guardamos los resultados en cache local
    if (filteredUsers.isNotEmpty) {
      // ignore: unawaited_futures
      SearchLocalCache.saveLastUserResults(filteredUsers);
    }
  }

  Future<void> _logSearchAnalytics(String query, int resultsCount) async {
    try {
      await FirebaseFirestore.instance.collection('search_logs').add({
        'query': query.trim().toLowerCase(),
        'resultsCount': resultsCount,
        'timestamp': FieldValue.serverTimestamp(),
        'userId': _currentUser?.id,
      });
    } catch (e) {
      debugPrint('Error logging search: $e');
    }
  }

  Future<void> _applySearch() async {
    final q = _queryCtrl.text.trim();
    if (q.isEmpty) return;

    final resultsCount = _searchResults.length;
    final isZeroResult = resultsCount == 0;

    // 1) Log en Firestore
    await _logSearchAnalytics(q, resultsCount);

    // 2) Log en Firebase Analytics
    await _analytics.logEvent(
      name: 'search_users',
      parameters: {
        'query': q.toLowerCase(),
        'results_count': resultsCount,
        'zero_result': isZeroResult,
      },
    );

    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Searching: "$q" ($resultsCount results)')),
    );
  }

  @override
  Widget build(BuildContext context) {
    final labelStyle = Theme.of(
      context,
    ).textTheme.bodyMedium?.copyWith(color: kAmber);

    final queryText = _queryCtrl.text.trim().toLowerCase();

    final fallbackResults = _cachedResults
        .where((s) => s.displayName.toLowerCase().contains(queryText))
        .toList();

    return SafeArea(
      child: Column(
        children: [
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Search', style: labelStyle),
                  const SizedBox(height: 8),

                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Expanded(
                        child: _shadowWrap(
                          TextField(
                            controller: _queryCtrl,
                            textInputAction: TextInputAction.search,
                            onSubmitted: (_) => _applySearch(),
                            inputFormatters: [
                              LengthLimitingTextInputFormatter(100),
                            ],
                            decoration: _pillInput(),
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),

                      _shadowWrap(
                        Material(
                          color: Colors.white,
                          shape: const CircleBorder(),
                          child: InkWell(
                            customBorder: const CircleBorder(),
                            onTap: () =>
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                    content: Text('Filtros (pendiente)'),
                                  ),
                                ),
                            child: const Padding(
                              padding: EdgeInsets.all(12),
                              child: Icon(Icons.tune, color: Colors.black87),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 24),

                  if (_searchResults.isNotEmpty) ...[
                    Text('Results', style: labelStyle),
                    const SizedBox(height: 12),
                    ..._searchResults.map(
                      (user) => SearchCard(
                        title: user.displayName,
                        score: _userScores[user],
                        leading: UserPfpCached(
                          uid: user.id,
                          radius: 18,
                        ),
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (_) => UserProfile(
                                userId: user.id,
                              ),
                            ),
                          );
                        },
                      ),
                    ),
                    const SizedBox(height: 24),
                  ],

                  if (_searchResults.isEmpty &&
                      queryText.isNotEmpty &&
                      fallbackResults.isNotEmpty) ...[
                    Text('Last results (cached)', style: labelStyle),
                    const SizedBox(height: 12),
                    ...fallbackResults.map(
                      (summary) => SearchCard(
                        title: summary.displayName,
                        leading: UserPfpCached(
                          uid: summary.uid,
                          radius: 18,
                        ),
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (_) => UserProfile(
                                userId: summary.uid,
                              ),
                            ),
                          );
                        },
                      ),
                    ),
                    const SizedBox(height: 24),
                  ],

                  if (_searchResults.isEmpty &&
                      queryText.isNotEmpty &&
                      fallbackResults.isEmpty) ...[
                    Text('No results found', style: labelStyle),
                    const SizedBox(height: 8),
                    const Text(
                      'Try searching for another name or check the spelling.',
                      style: TextStyle(fontSize: 12),
                    ),
                    const SizedBox(height: 24),
                  ],

                  // “Recientes”
                  Text('Recent searches', style: labelStyle),
                  const SizedBox(height: 12),
                  ..._recents.map(
                    (title) => SearchCard(
                      title: title,
                      isRecent: true,
                    ),
                  ),
                  // Botón debug para ver analytics de búsqueda
                  Align(
                    alignment: Alignment.centerRight,
                    child: TextButton(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const SearchAnalyticsDebugPage(),
                          ),
                        );
                      },
                      child: const Text(
                        'Open search analytics (debug)',
                        style: TextStyle(fontSize: 12),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class SearchCard extends StatelessWidget {
  const SearchCard({
    super.key,
    required this.title,
    this.score,
    this.isRecent = false,
    this.leading,
    this.onTap,
  });

  final String title;
  final bool isRecent;
  final double? score;

  final Widget? leading;

  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          if (isRecent)
            const Icon(
              Icons.access_time,
              color: Colors.black45,
            ),
          if (isRecent) const SizedBox(width: 8),
          Expanded(
            child: Material(
              color: Colors.white,
              elevation: 4,
              shadowColor: kShadowCol,
              borderRadius: BorderRadius.circular(12),
              child: InkWell(
                borderRadius: BorderRadius.circular(12),
                onTap:
                    onTap ??
                    () => ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text('Opening "$title"'),
                      ),
                    ),
                child: Padding(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 12,
                  ),
                  child: Row(
                    children: [
                      leading ??
                          CircleAvatar(
                            radius: 16,
                            backgroundColor: Colors.purple.shade200,
                            child: const Icon(
                              Icons.blur_on,
                              size: 18,
                              color: Colors.white,
                            ),
                          ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          title,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            fontSize: 14,
                          ),
                        ),
                      ),
                      if (score != null)
                        Expanded(
                          child: Text(
                            score!.toStringAsPrecision(3),
                            textAlign: TextAlign.end,
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// ---- UI helpers ----
InputDecoration _pillInput() {
  return InputDecoration(
    filled: true,
    fillColor: Colors.white,
    isDense: true,
    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kPillRadius),
      borderSide: const BorderSide(color: kAmber, width: 2),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kPillRadius),
      borderSide: const BorderSide(color: kAmber, width: 3),
    ),
  );
}

Widget _shadowWrap(Widget child) {
  return Container(
    decoration: BoxDecoration(
      borderRadius: BorderRadius.circular(kPillRadius),
      boxShadow: const [
        BoxShadow(
          color: kShadowCol,
          offset: Offset(0, 6),
          blurRadius: 12,
        ),
      ],
    ),
    child: child,
  );
}
