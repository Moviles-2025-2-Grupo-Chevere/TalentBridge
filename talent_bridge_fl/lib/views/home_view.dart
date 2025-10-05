import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/views/credits/credits.dart';
import 'package:talent_bridge_fl/views/leader_feed/leader_feed.dart';
import 'package:talent_bridge_fl/views/main-feed/main_feed.dart';
import 'package:talent_bridge_fl/views/my-profile/my_profile.dart';
import 'package:talent_bridge_fl/views/org-profile/org_profile.dart';
import 'package:talent_bridge_fl/views/project/project_view.dart';
import 'package:talent_bridge_fl/views/saved-projects/saved_projects.dart';
import 'package:talent_bridge_fl/views/search/search.dart';
import 'package:talent_bridge_fl/views/user-profile/user_profile.dart';

const kBg = Color(0xFFFEF7E6);

class HomeView extends StatefulWidget {
  const HomeView({super.key});

  @override
  State<HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> {
  int _selectedPageIdx = 0;


  var mainViews = [
    MainViewItem(widget: MainFeed(), icon: Icon(Icons.home_outlined), label: "Home"),
    MainViewItem(widget: Search(), icon: Icon(Icons.search), label: 'Search'),
    MainViewItem(widget: SavedProjects(), icon: Icon(Icons.favorite_outline), label: 'Saved'),
    MainViewItem(widget: MyProfile(), icon: Icon(Icons.person_outline), label: 'Profile'),

  ];
  
  void _selectPage(int idx){
    setState(() {
      _selectedPageIdx = idx;
    });
  }

  @override
  Widget build(BuildContext context) {
    Widget activePage = mainViews[_selectedPageIdx].widget;

    return Scaffold(
      backgroundColor: kBg,
      appBar: AppBar(
        title: Image.asset(
          'assets/images/MainAppIcon.png', 
          height: 40, 
          fit: BoxFit.contain,
        ),
        actions: [
          IconButton(
            icon: Icon(Icons.menu),
            onPressed: () {
              print('Menu tapped');
            },
          ),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        onTap: _selectPage,
        type: BottomNavigationBarType.fixed,
        items: mainViews.map((i) => BottomNavigationBarItem(icon: i.icon, label: i.label)).toList(),
      ),
      body: activePage,
    );
  }
}

class MainViewItem {
  MainViewItem({required this.widget, required this.icon, required this.label});

  final Widget widget;
  final Icon icon;
  final String label;

}