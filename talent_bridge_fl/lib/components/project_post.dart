import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';

class ProjectPost extends StatelessWidget {
  const ProjectPost({super.key, required this.project});

  final ProjectEntity project;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 8),
        child: Column(
          children: [
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
                TextButton(onPressed: () {}, child: Text('Comentarios')),
                TextButton(onPressed: () {}, child: Text('Guardar')),
                TextButton(onPressed: () {}, child: Text('Aplicar')),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
