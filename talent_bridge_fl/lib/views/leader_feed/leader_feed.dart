import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/user_list.dart';
import 'package:talent_bridge_fl/data/user_service.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';

class LeaderFeed extends StatelessWidget {
  LeaderFeed({super.key});

  final userService = UserService();
  @override
  Widget build(BuildContext context) {
    List<UserEntity> users = [];
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'Explore Students',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 36,
                color: Color.fromARGB(255, 255, 195, 0),
              ),
            ),
          ],
        ),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 0),
            child: UserList(
              users: users,
            ),
          ),
        ),
      ],
    );
  }
}
