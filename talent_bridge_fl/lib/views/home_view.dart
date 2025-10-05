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
  final _prototypeViews = {
    'Login': Login(),
    'Sign Up': Signup(),
    'Main Feed': MainFeed(),
    'Leader Feed': LeaderFeed(),
    'My Profile': MyProfile(),
    'User Profile': UserProfile(),
    'Organization Profile': OrgProfile(),
    'Project Detail': ProjectView(),
    'Search': Search(),
    'Credits': Credits(),
  };

  Widget? _currentView;

  void updateWidget(Widget w) {
    setState(() {
      _currentView = w;
    });
  }

  @override
  void initState() {
    super.initState();
    _currentView = PrototypeViewList(
      prototypeViews: _prototypeViews,
      updateWidget: updateWidget,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Image.asset(
          'assets/images/MainAppIcon.png', // Replace with your image path
          height: 40, // Adjust height as needed
          fit: BoxFit.contain,
        ),
        actions: [
          IconButton(
            icon: Icon(Icons.arrow_back),
            onPressed: () {
              Navigator.pop(context);
            },
          ),
          IconButton(
            icon: Icon(Icons.menu),
            onPressed: () {
              print('Menu tapped');
            },
          ),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed, // Add this line
        items: [
          BottomNavigationBarItem(
            icon: Icon(Icons.add_home),
            label: 'Home',
          ),
          BottomNavigationBarItem(icon: Icon(Icons.search), label: 'Search'),
          BottomNavigationBarItem(icon: Icon(Icons.menu), label: 'Menu'),
          BottomNavigationBarItem(
            icon: Icon(Icons.favorite_outline),
            label: 'Saved',
          ),
          // BottomNavigationBarItem(icon: Icon(Icons.code), label: 'Prototype'),
        ],
      ),
      body: _currentView,
    );
  }
}

class PrototypeViewList extends StatelessWidget {
  const PrototypeViewList({
    super.key,
    required this.prototypeViews,
    required this.updateWidget,
  });

  final Map<String, Widget> prototypeViews;

  final void Function(Widget w) updateWidget;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          ...prototypeViews.entries.map(
            (entry) => ElevatedButton(
              onPressed: () {
                updateWidget(entry.value);
              },
              child: Text(entry.key),
            ),
          ),
        ],
      ),
    );
  }
}
