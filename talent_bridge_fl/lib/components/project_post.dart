import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/project_post_image.dart';
import 'package:talent_bridge_fl/components/project_post_pfp.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/views/user-profile/user_profile.dart';

class ProjectPost extends StatelessWidget {
  ProjectPost({
    super.key,
    required this.project,
    required this.showApplyModal,
    required this.showSaveModal,
    required this.showRemoveModal,
  });

  final ProjectEntity project;
  final void Function(String, String, String) showApplyModal;
  final void Function(ProjectEntity) showSaveModal;
  final void Function(ProjectEntity) showRemoveModal;
  @override
  Widget build(BuildContext context) {
    final firebaseService = FirebaseService();
    var displayName = (project.createdBy?.displayName ?? '').isNotEmpty
        ? project.createdBy!.displayName
        : 'Anon user';
    var minutesAgo = project.timeAgo;

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
                      final uid = project.createdById;
                      debugPrint(
                        '[FEED] tap avatar -> open UserProfile(userId=$uid)',
                      );
                      if (uid.isEmpty) return;
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => UserProfile(userId: uid),
                        ),
                      );
                    },

                    customBorder: const CircleBorder(),
                    child: ProjectPostPfp(uid: project.createdById),
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
                        "$displayName · $minutesAgo",
                        style: TextStyle(fontWeight: FontWeight.w300),
                      ),
                      Text(project.description),
                    ],
                  ),
                ),
              ],
            ),
            ProjectPostImage(
              project: project, //Gets the image by project ID
            ),
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
                TextButton(onPressed: () {}, child: Text('Comments')),
                if (!project.isFavorite)
                  TextButton(
                    onPressed: () => showSaveModal(project),
                    child: Text('Save'),
                  ),
                if (project.isFavorite)
                  TextButton(
                    onPressed: () => showRemoveModal(project),
                    child: Text('Remove'),
                  ),
                TextButton(
                  onPressed: () {
                    final currentUserId = firebaseService.currentUid() ?? "";
                    showApplyModal(
                      currentUserId,
                      project.createdById,
                      project.id ?? "",
                    );
                  },
                  child: Text('Apply'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
