import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/views/credits/credits.dart';
import 'package:talent_bridge_fl/views/leader_feed/leader_feed.dart';
import 'package:talent_bridge_fl/views/login/login.dart';
import 'package:talent_bridge_fl/views/main-feed/main_feed.dart';
import 'package:talent_bridge_fl/views/my-profile/my_profile.dart';
import 'package:talent_bridge_fl/views/org-profile/org_profile.dart';
import 'package:talent_bridge_fl/views/project/project_view.dart';
import 'package:talent_bridge_fl/views/search/search.dart';
import 'package:talent_bridge_fl/views/signup/signup.dart';
import 'package:talent_bridge_fl/views/user-profile/user_profile.dart';

class HomeView extends StatefulWidget {
  const HomeView({super.key});

  @override
  State<HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
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
        type: BottomNavigationBarType.fixed,
        items: [
          BottomNavigationBarItem(
            icon: Icon(Icons.add_home),
            label: 'Home',
          ),
          BottomNavigationBarItem(icon: Icon(Icons.search), label: 'Search'),
          BottomNavigationBarItem(
            icon: Icon(Icons.favorite_outline),
            label: 'Saved',
          ),
          BottomNavigationBarItem(icon: Icon(Icons.menu), label: 'Menu'),
          // BottomNavigationBarItem(icon: Icon(Icons.code), label: 'Prototype'),
        ],
      ),
      body: MainFeed(),
    );
  }
}
