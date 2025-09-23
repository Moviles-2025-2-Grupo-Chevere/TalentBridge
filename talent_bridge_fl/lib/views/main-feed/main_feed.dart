import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/project_list.dart';
import 'package:talent_bridge_fl/components/project_post.dart';
import 'package:talent_bridge_fl/data/project_service.dart';

class MainFeed extends StatelessWidget {
  MainFeed({super.key});

  final projectService = ProjectService();
  @override
  Widget build(BuildContext context) {
    var projects = projectService.getProjects();
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
          child: ProjectList(projects: projects),
        ),
      ],
    );
  }
}
