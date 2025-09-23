import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/project_post.dart';
import 'package:talent_bridge_fl/components/submit_alert.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';

class ProjectList extends StatelessWidget {
  const ProjectList({super.key, required this.projects});

  final List<ProjectEntity> projects;

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: projects.length,
      itemBuilder: (ctx, index) => Dismissible(
        key: ValueKey(projects[index]),
        onDismissed: (direction) {},
        child: ProjectPost(
          project: projects[index],
          showApplyModal: () {
            showDialog(
              context: context,
              builder: (BuildContext context) {
                return SubmitAlert();
              },
            );
          },
        ),
      ),
    );
  }
}
