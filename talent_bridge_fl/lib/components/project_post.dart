import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/views/user-profile/user_profile.dart';

class ProjectPost extends StatelessWidget {
  const ProjectPost({
    super.key,
    required this.project,
    required this.showApplyModal,
  });

  final ProjectEntity project;
  final void Function() showApplyModal;

  @override
  Widget build(BuildContext context) {
    var profilePictureUrl = project.createdBy.photoUrl;
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
                  child: InkWell(
                    splashColor: Colors.white,
                    onTap: () {
                      Navigator.of(
                        context,
                      ).push(
                        MaterialPageRoute(
                          builder: (ctx) =>
                              Scaffold(appBar: AppBar(), body: UserProfile()),
                        ),
                      );
                    },
                    customBorder: const CircleBorder(),
                    child: CircleAvatar(
                      radius: 24,
                      backgroundImage: profilePictureUrl != null
                          ? AssetImage(profilePictureUrl)
                          : null,
                    ),
                  ),
                ),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        project.title,
                        style: TextStyle(fontWeight: FontWeight.bold),
                      ),
                      Text(
                        "${project.createdBy.displayName} · 5m",
                        style: TextStyle(fontWeight: FontWeight.w300),
                      ),
                      Text(project.description),
                    ],
                  ),
                ),
              ],
            ),
            if (project.imgUrl != null) Image.asset(project.imgUrl!),
            Wrap(
              spacing: 4,
              children: [
                ...project.skills.map(
                  (v) => OutlinedButton(onPressed: () {}, child: Text(v)),
                ),
              ],
            ),
            Wrap(
              spacing: 4,
              children: [
                TextButton(onPressed: () {}, child: Text('Comentarios')),
                TextButton(onPressed: () {}, child: Text('Guardar')),
                TextButton(onPressed: showApplyModal, child: Text('Aplicar')),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
