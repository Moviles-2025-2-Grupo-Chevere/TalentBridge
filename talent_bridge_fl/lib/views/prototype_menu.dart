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

class PrototypeMenu extends StatefulWidget {
  const PrototypeMenu({super.key});

  @override
  State<PrototypeMenu> createState() => _PrototypeMenuState();
}

class _PrototypeMenuState extends State<PrototypeMenu> {
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
  // var _currentView = PrototypeViewList(prototypeViews: _prototypeViews);

  @override
  void initState() {
    // TODO: implement initState

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Talent Bridge'),
      ),
      bottomNavigationBar: BottomNavigationBar(
        items: [
          BottomNavigationBarItem(icon: Icon(Icons.add_home), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.search), label: 'Search'),
          BottomNavigationBarItem(
            icon: Icon(Icons.favorite_outline),
            label: 'Saved',
          ),
          // BottomNavigationBarItem(icon: Icon(Icons.code), label: 'Prototype'),
        ],
      ),
      body: PrototypeViewList(prototypeViews: _prototypeViews),
    );
  }
}

class PrototypeViewList extends StatelessWidget {
  const PrototypeViewList({
    super.key,
    required this.prototypeViews,
  });

  final Map<String, StatelessWidget> prototypeViews;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          ...prototypeViews.entries.map(
            (entry) => ElevatedButton(onPressed: () {}, child: Text(entry.key)),
          ),
        ],
      ),
    );
  }
}
