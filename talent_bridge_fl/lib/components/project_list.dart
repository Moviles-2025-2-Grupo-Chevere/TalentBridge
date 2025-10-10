import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/project_post.dart';
import 'package:talent_bridge_fl/components/submit_alert_db.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class ProjectList extends StatelessWidget {
  const ProjectList({super.key, required this.projects});

  final List<ProjectEntity> projects;

  @override
  Widget build(BuildContext context) {
    final firebaseService = FirebaseService();
    return ListView.builder(
      itemCount: projects.length,
      itemBuilder: (ctx, index) => Dismissible(
        key: ValueKey(projects[index]),
        onDismissed: (direction) {},
        child: ProjectPost(
          project: projects[index],
          showApplyModal: (String userId, String projectId) {
            showDialog(
              context: context,
              builder: (BuildContext context) {
                return SubmitAlertDb(
                  userId: userId,
                  projectId: projectId,
                  onConfirm: () async {
                    try {
                      await firebaseService.addProjectToApplications(
                        userId: userId,
                        projectId: projectId,
                      );
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text('Application submitted successfully!'),
                        ),
                      );
                    } catch (e) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text('Error submitting application: $e'),
                        ),
                      );
                    }
                    Navigator.of(context).pop(); // Close the dialog
                  },
                );
              },
            );
          },
        ),
      ),
    );
  }
}
