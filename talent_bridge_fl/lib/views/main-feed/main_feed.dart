import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/project_list.dart';
import 'package:talent_bridge_fl/data/project_service.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class MainFeed extends StatelessWidget {
  MainFeed({super.key});

  final projectService = ProjectService();
  final _firebaseService = FirebaseService();
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'Explore Projects',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 36,
                color: Color.fromARGB(255, 255, 195, 0),
              ),
            ),
          ],
        ),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 0),
            child: FutureBuilder<List<ProjectEntity>>(
              future: _firebaseService.getAllProjects(),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return Center(child: CircularProgressIndicator());
                } else if (snapshot.hasError) {
                  return Center(child: Text('Error: ${snapshot.error}'));
                } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                  return Center(child: Text('No projects available'));
                } else {
                  return ProjectList(projects: snapshot.data!);
                }
              },
            ),
          ),
        ),
      ],
    );
  }
}
