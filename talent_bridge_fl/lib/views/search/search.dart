import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

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

  // User data
  List<UserEntity> _allUsers = [];
  UserEntity? _currentUser;

  // Search results
  List<UserEntity> _searchResults = [];

  // Fake “recent” items for the UI
  final _recents = const <String>[
    'Daniel Triviño',
    'ROBOCOL',
    'Proyectos Inteligencia Artificial',
  ];

  @override
  void initState() {
    super.initState();
    _loadUserData();
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
      });
      return;
    }
    final projects = _currentUser?.projects ?? [];
    final skills = projects.expand((i) => i.skills).toList();
    final frequencies = skills.fold<Map<String, int>>(
      {},
      (map, item) => map..update(item, (v) => v + 1, ifAbsent: () => 1),
    );
    final weights = frequencies.map((s, i) => MapEntry(s, i / skills.length));
    final filteredUsers = _allUsers
        .where((u) => u.displayName.toLowerCase().contains(q))
        .toList();
    final scores = filteredUsers.fold<Map<UserEntity, double>>(
      {},
      (map, item) => map
        ..update(item, (v) {
          double score = 0;
          for (var element in item.skillsOrTopics ?? [] as List<String>) {
            score += weights[element] ?? 0;
          }
          return score;
        }, ifAbsent: () => 0),
    );
    filteredUsers.sort((a, b) => scores[a]!.compareTo(scores[b]!));
    setState(() {
      _searchResults = filteredUsers;
    });
  }

  void _applySearch() {
    final q = _queryCtrl.text.trim();
    if (q.isEmpty) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Buscando: "$q"')),
    );
    // TODO: hook real search here
  }

  @override
  Widget build(BuildContext context) {
    final labelStyle = Theme.of(
      context,
    ).textTheme.bodyMedium?.copyWith(color: kAmber);

    return SafeArea(
      child: Column(
        children: [
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Text(
                  //   _currentUser != null ? _currentUser!.displayName : '?',
                  // ),
                  // Label “Buscar”
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
                            decoration: _pillInput(), // sin icono de lupa
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),

                      // Botón redondo de filtros
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

                  // Search results
                  if (_searchResults.isNotEmpty) ...[
                    Text('Results', style: labelStyle),
                    const SizedBox(height: 12),
                    ..._searchResults.map(
                      (user) => SearchCard(title: user.displayName),
                    ),
                    const SizedBox(height: 24),
                  ],

                  // “Recientes”
                  Text('Recent searches', style: labelStyle),
                  const SizedBox(height: 12),

                  // Lista de recientes (icono reloj + tarjeta pill)
                  ..._recents.map(
                    (title) => SearchCard(title: title),
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
  });

  final String title;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          const Icon(
            Icons.access_time,
            color: Colors.black45,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Material(
              color: Colors.white,
              elevation: 4,
              shadowColor: kShadowCol,
              borderRadius: BorderRadius.circular(12),
              child: InkWell(
                borderRadius: BorderRadius.circular(12),
                onTap: () => ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text('Abrir "$title"'),
                  ),
                ),
                child: Padding(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 12,
                  ),
                  child: Row(
                    children: [
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
