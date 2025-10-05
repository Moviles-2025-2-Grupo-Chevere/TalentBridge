import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/profile_drawer.dart';
import 'package:talent_bridge_fl/views/credits/credits.dart';
import 'package:talent_bridge_fl/views/leader_feed/leader_feed.dart';
import 'package:talent_bridge_fl/views/main-feed/main_feed.dart';
import 'package:talent_bridge_fl/views/my-profile/my_profile.dart';
// import 'package:talent_bridge_fl/views/project/project_view.dart';
// import 'package:talent_bridge_fl/views/saved-projects/saved_projects.dart';
import 'package:talent_bridge_fl/views/search/search.dart';

const kBg = Color(0xFFFEF7E6);

class HomeView extends StatefulWidget {
  const HomeView({super.key});

  @override
  State<HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> {
  int _selectedPageIdx = 0;

  void _selectPage(int idx) {
    setState(() {
      _selectedPageIdx = idx;
    });
  }

  @override
  Widget build(BuildContext context) {
    final mainViews = [
      MainViewItem(
        title: 'Home',
        widget: MainFeed(),
        icon: Icon(Icons.home_outlined),
        label: "Home",
        actions: [
          IconButton(
            icon: Icon(Icons.people_outline),
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (ctx) => Scaffold(
                    backgroundColor: kBg,
                    appBar: AppBar(
                      title: Text('Featured Students'),
                    ),
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
        widget: Search(),
        icon: Icon(Icons.search),
        label: 'Search',
        actions: [],
      ),
      MainViewItem(
        title: 'My Profile',
        widget: MyProfile(),
        icon: Icon(Icons.person_outline),
        label: 'Profile',
        actions: [],
        drawer: ProfileDrawer(
          onTapLogOut: () {},
          onTapCredits: () {
            Navigator.of(context).push(
              MaterialPageRoute(builder: (ctx) => Credits()),
            );
          },
        ),
      ),
    ];
    final selectedView = mainViews[_selectedPageIdx];
    Widget activePage = selectedView.widget;

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
            const SizedBox(
              width: 8,
            ),
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
