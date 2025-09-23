import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/user_card.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';

class UserList extends StatelessWidget {
  const UserList({super.key, required this.users});

  final List<UserEntity> users;
  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: users.length,
      itemBuilder: (ctx, index) => Dismissible(
        key: ValueKey(users[index]),
        child: UserCard(user: users[index]),
      ),
    );
  }
}
