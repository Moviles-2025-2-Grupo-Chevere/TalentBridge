import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';

class ProjectPost extends StatelessWidget {
  const ProjectPost({super.key, required this.project});

  final ProjectEntity project;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(padding: const EdgeInsets.all(16)),
    );
  }
}
