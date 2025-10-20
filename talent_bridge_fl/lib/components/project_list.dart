import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/project_post.dart';
import 'package:talent_bridge_fl/components/submit_alert_db.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class ProjectList extends StatelessWidget {
  ProjectList({super.key, required this.projects});
  final firebaseService = FirebaseService();

  final List<ProjectEntity> projects;

  void submitProjectApplication(
    BuildContext context,
    String userId,
    String projectId,
  ) async {
    try {
      await firebaseService.addProjectToApplications(
        userId: userId,
        projectId: projectId,
      );
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Application submitted successfully!'),
          ),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error submitting application: $e'),
          ),
        );
      }
    }
    if (context.mounted) {
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
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
              builder: (BuildContext dialogContext) => SubmitAlertDb(
                userId: userId,
                projectId: projectId,
                onConfirm: () =>
                    submitProjectApplication(context, userId, projectId),
              ),
            );
          },
        ),
      ),
    );
  }
}
