import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/components/profile_drawer.dart';
import 'package:talent_bridge_fl/providers/notification_provider.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/views/credits/credits.dart';
import 'package:talent_bridge_fl/views/leader_feed/leader_feed.dart';
import 'package:talent_bridge_fl/views/login/login.dart';
import 'package:talent_bridge_fl/views/main-feed/main_feed.dart';
import 'package:talent_bridge_fl/views/my-profile/my_profile.dart';
import 'package:talent_bridge_fl/views/saved-projects/saved_projects.dart';
import 'package:talent_bridge_fl/views/search/search.dart';

// ⬇️ NUEVO
import 'package:firebase_performance/firebase_performance.dart';

const kBg = Color(0xFFFEF7E6);

class HomeView extends ConsumerStatefulWidget {
  const HomeView({super.key});

  @override
  ConsumerState<HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends ConsumerState<HomeView> {
  final _fb = FirebaseService();
  int _selectedPageIdx = 0;

  // ---- BQ: Trace TTFC Home (mínimo y seguro contra rebuilds) ----
  static Trace? _ttfcHome;
  static bool _started = false;
  static bool _stopped = false;

  @override
  void initState() {
    super.initState();

    // arranca el trace solo una vez
    if (!_started) {
      _started = true;
      _ttfcHome = FirebasePerformance.instance.newTrace('ttfc_home')
        ..putAttribute('screen', 'Home') // etiqueta opcional
        ..start();
    }

    // lo detenemos cuando el primer frame del Home ya se pintó
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_stopped) {
        _stopped = true;
        _ttfcHome?.stop();
      }
    });
  }
  // ---------------------------------------------------------------

  void _selectPage(int idx) {
    setState(() {
      _selectedPageIdx = idx;
    });
  }

  @override
  Widget build(BuildContext context) {
    ref.listen(
      notificationProvider,
      (previous, next) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Row(
              children: const [
                Icon(Icons.notification_important_outlined),
                SizedBox(width: 8),
              ],
            ),
          ),
        );
      },
    );

    final mainViews = [
      MainViewItem(
        title: 'Home',
        widget: MainFeed(),
        icon: const Icon(Icons.home_outlined),
        label: "Home",
        actions: [
          IconButton(
            icon: const Icon(Icons.bookmark_outline),
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (ctx) => Scaffold(
                    appBar: AppBar(title: const Text('Saved Projects')),
                    body: SavedProjects(),
                  ),
                ),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.people_outline),
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (ctx) => Scaffold(
                    backgroundColor: kBg,
                    appBar: AppBar(title: const Text('Featured Students')),
                    body: LeaderFeed(),
                  ),
                ),
              );
            },
          ),
        ],
      ),
      MainViewItem(
        title: 'Search',
        widget: const Search(),
        icon: const Icon(Icons.search),
        label: 'Search',
        actions: const [],
      ),
      MainViewItem(
        title: 'My Profile',
        widget: const MyProfile(),
        icon: const Icon(Icons.person_outline),
        label: 'Profile',
        actions: const [],
        drawer: ProfileDrawer(
          onTapLogOut: () async {
            await _fb.signOut();
            if (context.mounted) {
              Navigator.of(context).pushAndRemoveUntil(
                MaterialPageRoute(builder: (_) => const Login()),
                (_) => false,
              );
            }
          },
          onTapCredits: () {
            Navigator.of(context).push(
              MaterialPageRoute(builder: (ctx) => const Credits()),
            );
          },
        ),
      ),
    ];

    final selectedView = mainViews[_selectedPageIdx];
    final Widget activePage = selectedView.widget;

    return Scaffold(
      backgroundColor: kBg,
      appBar: AppBar(
        title: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Image.asset(
              'assets/images/MainAppIcon.png',
              height: 40,
              fit: BoxFit.contain,
            ),
            const SizedBox(width: 8),
            Text(selectedView.title),
          ],
        ),
        actions: selectedView.actions,
      ),
      endDrawer: selectedView.drawer,
      bottomNavigationBar: BottomNavigationBar(
        onTap: _selectPage,
        currentIndex: _selectedPageIdx,
        type: BottomNavigationBarType.fixed,
        items: mainViews
            .map((i) => BottomNavigationBarItem(icon: i.icon, label: i.label))
            .toList(),
      ),
      body: activePage,
    );
  }
}

class MainViewItem {
  const MainViewItem({
    required this.title,
    required this.widget,
    required this.icon,
    required this.label,
    required this.actions,
    this.drawer,
  });

  final String title;
  final Widget widget;
  final Icon icon;
  final String label;
  final List<Widget> actions;
  final Widget? drawer;
}
