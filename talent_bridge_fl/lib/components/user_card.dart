import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';

class UserCard extends StatelessWidget {
  const UserCard({super.key, required this.user});

  final UserEntity user;

  @override
  Widget build(BuildContext context) {
    var profilePictureUrl = user.profilePictureUrl;
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 8),
        child: Column(
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: EdgeInsets.symmetric(vertical: 4, horizontal: 2),
                  child: CircleAvatar(
                    radius: 24,
                    backgroundImage: profilePictureUrl != null
                        ? AssetImage(profilePictureUrl)
                        : null,
                  ),
                ),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        user.name,
                        style: TextStyle(fontWeight: FontWeight.bold),
                      ),
                      Text(
                        user.mainProgram,
                        style: TextStyle(fontWeight: FontWeight.w300),
                      ),
                      Text(user.description),
                    ],
                  ),
                ),
              ],
            ),
            Wrap(
              spacing: 4,
              children: [
                ...user.skills.map(
                  (v) => OutlinedButton(onPressed: () {}, child: Text(v)),
                ),
              ],
            ),
            Wrap(
              spacing: 4,
              children: [
                TextButton(onPressed: () {}, child: Text('Guardar')),
                TextButton(onPressed: () {}, child: Text('Contactar')),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
