import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/components/project_post.dart';
import 'package:talent_bridge_fl/components/submit_alert_db.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/providers/upload_queue_apply_project.dart';
import 'package:talent_bridge_fl/services/db_service.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class ProjectList extends ConsumerStatefulWidget {
  const ProjectList({super.key, required this.projects});

  final List<ProjectEntity> projects;

  @override
  ConsumerState<ProjectList> createState() => _ProjectListState();
}

class _ProjectListState extends ConsumerState<ProjectList> {
  final firebaseService = FirebaseService();
  final dbService = DbService();

  late final List<ProjectEntity> projects;

  Future<void> submitProjectApplication(
    BuildContext context,
    String currentUserId,
    String createdById,
    String projectId,
  ) async {
    // Capture the ScaffoldMessenger before any async operations
    final scaffoldMessenger = ScaffoldMessenger.of(context);
    final navigator = Navigator.of(context);

    try {
      final result = await ref
          .read(projectApplyUploadProvider.notifier)
          .enqueueProjectApplyUpload(
            currentUserId,
            projectId,
            createdById,
          );

      // Close the dialog first
      navigator.pop();

      // Then show the appropriate message
      if (!result) {
        scaffoldMessenger.showSnackBar(
          const SnackBar(
            content: Text('The application will be sent later, when online'),
          ),
        );
      } else {
        debugPrint('Project application submitted successfully');
        scaffoldMessenger.showSnackBar(
          const SnackBar(
            content: Text('The application has been submitted successfully'),
          ),
        );
      }
    } catch (e) {
      debugPrint(e.toString());
      navigator.pop(); // Close dialog on error too
      scaffoldMessenger.showSnackBar(
        SnackBar(
          content: Text('Error submitting application: $e'),
        ),
      );
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

  void _showApplyModal(
    String currentUserId,
    String createdById,
    String projectId,
  ) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext dialogContext) => SubmitAlertDb(
        userId: currentUserId,
        projectId: projectId,
        onConfirm: () => submitProjectApplication(
          context,
          currentUserId,
          createdById,
          projectId,
        ),
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
    ref.listen(
      projectApplyUploadProvider,
      (prev, next) {
        if (prev != null && prev.isNotEmpty && next.length < prev.length) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text("Application uploaded successfully"),
              backgroundColor: Colors.green,
            ),
          );
        }
      },
    );

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
          showApplyModal: _showApplyModal,
        ),
      ),
    );
  }
}
