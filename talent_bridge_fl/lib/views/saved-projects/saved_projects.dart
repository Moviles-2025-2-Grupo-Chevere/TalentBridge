import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/project_list.dart';
import 'package:talent_bridge_fl/services/db_service.dart';

class SavedProjects extends StatelessWidget {
  const SavedProjects({super.key});

  final dbService = const DbService();

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: dbService.getSavedProjects(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          return ProjectList(projects: snapshot.data!);
        } else {
          return Center(child: CircularProgressIndicator());
        }
      },
    );
  }
}
