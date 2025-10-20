import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/db_service.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/views/user-profile/user_profile.dart';

class ProjectPost extends StatelessWidget {
  const ProjectPost({
    super.key,
    required this.project,
    required this.showApplyModal,
    required this.removeFromList,
  });

  final ProjectEntity project;
  final void Function(String, String) showApplyModal;
  final void Function(ProjectEntity) removeFromList;
  final dbService = const DbService();

  Future<void> onSaveProject(BuildContext context) async {
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

  Future<void> onRemoveProject(BuildContext context) async {
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

  void showSaveModal(BuildContext context) {
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
              onSaveProject(context);
              Navigator.of(context).pop();
            },
            label: Text("Save"),
            icon: Icon(Icons.save),
          ),
        ],
      ),
    );
  }

  void showRemoveModal(BuildContext context) {
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
              onRemoveProject(context);
              Navigator.of(context).pop();
            },
            label: Text("Remove"),
            icon: Icon(Icons.save),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final firebaseService = FirebaseService();
    var profilePictureUrl = project.createdBy?.photoUrl;
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
                          : AssetImage('assets/images/gumball.jpg'),
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
                        "${project.createdBy?.displayName ?? 'Project Manager'} · 5m",
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
                TextButton(onPressed: () {}, child: Text('Comments')),
                if (!project.isFavorite)
                  TextButton(
                    onPressed: () => showSaveModal(context),
                    child: Text('Save'),
                  ),
                if (project.isFavorite)
                  TextButton(
                    onPressed: () => showRemoveModal(context),
                    child: Text('Remove'),
                  ),
                TextButton(
                  onPressed: () {
                    final currentUserId = firebaseService.currentUid() ?? "";
                    showApplyModal(currentUserId, project.id ?? "");
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
