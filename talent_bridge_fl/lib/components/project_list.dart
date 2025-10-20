import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/project_post.dart';
import 'package:talent_bridge_fl/components/submit_alert_db.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/db_service.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class ProjectList extends StatefulWidget {
  const ProjectList({super.key, required this.projects});

  final List<ProjectEntity> projects;

  @override
  State<ProjectList> createState() => _ProjectListState();
}

class _ProjectListState extends State<ProjectList> {
  final firebaseService = FirebaseService();
  final dbService = const DbService();

  late final List<ProjectEntity> projects;

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

  void removeFromList(ProjectEntity p) {
    setState(() {
      projects.removeWhere((element) => element.id == p.id);
    });
  }

  Future<void> onSaveProject(ProjectEntity project) async {
    var scaffoldMessenger = ScaffoldMessenger.of(context);
    try {
      await dbService.insertSavedProject(project);
      if (context.mounted) {
        scaffoldMessenger.clearSnackBars();
        scaffoldMessenger.showSnackBar(
          SnackBar(content: Text('Project saved as favorite')),
        );
      }
    } catch (e) {
      if (context.mounted) {
        scaffoldMessenger.clearSnackBars();
        scaffoldMessenger.showSnackBar(
          SnackBar(content: Text('Error saving project to favorites')),
        );
      }
    }
  }

  Future<void> onRemoveProjectFromFavorites(ProjectEntity project) async {
    var scaffoldMessenger = ScaffoldMessenger.of(context);
    try {
      await dbService.removeSavedProject(project.id!);
      removeFromList(project);
      if (context.mounted) {
        scaffoldMessenger.clearSnackBars();
        scaffoldMessenger.showSnackBar(
          SnackBar(content: Text('Project removed from favorites')),
        );
      }
    } catch (e) {
      if (context.mounted) {
        scaffoldMessenger.clearSnackBars();
        scaffoldMessenger.showSnackBar(
          SnackBar(content: Text('Error removing project from favorites')),
        );
      }
    }
  }

  void showSaveModal(ProjectEntity p) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text("Save project"),
        content: Text("Save this project in your favorites?"),
        actions: [
          OutlinedButton.icon(
            onPressed: () {
              Navigator.of(context).pop();
            },
            label: Text("Cancel"),
            icon: Icon(Icons.cancel),
          ),
          FilledButton.icon(
            onPressed: () {
              onSaveProject(p);
              Navigator.of(context).pop();
            },
            label: Text("Save"),
            icon: Icon(Icons.save),
          ),
        ],
      ),
    );
  }

  void showRemoveFromFavoritesModal(ProjectEntity p) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text("Remove project"),
        content: Text("Remove this project from your favorites?"),
        actions: [
          OutlinedButton.icon(
            onPressed: () {
              Navigator.of(context).pop();
            },
            label: Text("Cancel"),
            icon: Icon(Icons.cancel),
          ),
          FilledButton.icon(
            onPressed: () {
              onRemoveProjectFromFavorites(p);
              Navigator.of(context).pop();
            },
            label: Text("Remove"),
            icon: Icon(Icons.remove),
          ),
        ],
      ),
    );
  }

  @override
  void initState() {
    super.initState();
    projects = widget.projects;
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: projects.length,
      itemBuilder: (ctx, index) => Dismissible(
        key: ValueKey(projects[index]),
        onDismissed: (direction) {
          if (direction == DismissDirection.endToStart &&
              projects[index].isFavorite) {
            onRemoveProjectFromFavorites(projects[index]);
          }
        },
        child: ProjectPost(
          project: projects[index],
          showRemoveModal: showRemoveFromFavoritesModal,
          showSaveModal: showSaveModal,
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
