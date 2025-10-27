import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/data/project_service.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/services/skills_service.dart';
import 'package:talent_bridge_fl/views/select_skills/select_skills.dart';

const darkBlue = Color(0xFF3E6990);

class AddProject extends StatefulWidget {
  const AddProject({super.key, required this.onAddProject});

  final void Function(ProjectEntity project) onAddProject;

  @override
  State<AddProject> createState() => _AddProjectState();
}

class _AddProjectState extends State<AddProject> {
  final firebaseService = FirebaseService();
  final projectService = ProjectService();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  // final _skillsController = TextEditingController();
  final _skills = SkillsService.getSkills();
  final _selectedSkills = HashSet<String>();

  void _submitData() {
    final title = _titleController.text;
    final description = _descriptionController.text;
    final List<String> skills = _selectedSkills.toList();
    final uid = firebaseService.currentUid();

    if (title.isEmpty || uid == null) {
      showDialog(
        context: context,
        builder: (ctx) => AlertDialog(
          title: const Text('Invalid input'),
          content: const Text(
            'Please make sure a valid title was entered',
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(ctx);
              },
              child: const Text('Okay'),
            ),
          ],
        ),
      );
      return;
    }

    widget.onAddProject(
      ProjectEntity(
        createdAt: DateTime.now(),
        createdById: uid,
        title: title,
        description: description,
        skills: skills,
      ),
    );
    Navigator.pop(context);
  }

  void _openSkillsView() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => PopScope(
          onPopInvokedWithResult: (didPop, result) {
            setState(() {});
          },
          child: Scaffold(
            appBar: AppBar(
              title: const Text("Select project Skills"),
            ),
            body: SelectSkills(
              skills: _skills,
              selectedSkills: _selectedSkills,
            ),
          ),
        ),
      ),
    );
  }

  void _removeSelectedSkill(String skill) {
    setState(() {
      _selectedSkills.remove(skill);
    });
  }

  void _getAnImage() {}

  @override
  Widget build(BuildContext context) {
    var titleField = TextField(
      controller: _titleController,
      decoration: const InputDecoration(
        label: Text('Title'),
      ),
    );
    var descriptionField = TextField(
      controller: _descriptionController,
      maxLines: null, // expands as user types
      keyboardType: TextInputType.multiline,
      decoration: InputDecoration(
        label: Text('Description'),
        hintText: "Write your comment...",
        border: OutlineInputBorder(),
      ),
    );
    return SizedBox(
      height: double.infinity,
      child: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 16),
          child: Column(
            children: [
              Text(
                'Add a Project',
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              titleField,
              SizedBox(height: 16),
              descriptionField,
              SizedBox(height: 16),
              Row(
                children: [
                  Text("Skills and Topics"),
                  SizedBox(
                    width: 12,
                  ),
                  IconButton(
                    onPressed: () => _openSkillsView(),
                    icon: Icon(Icons.add),
                  ),
                ],
              ),
              Wrap(
                spacing: 8,
                children: _selectedSkills
                    .map(
                      (e) => InputChip(
                        label: Text(e),
                        onDeleted: () => _removeSelectedSkill(e),
                      ),
                    )
                    .toList(),
              ),
              SizedBox(
                height: 16,
              ),
              Row(
                children: [
                  Text("Add a picture"),
                  SizedBox(
                    width: 12,
                  ),
                  IconButton(
                    onPressed: () => _getAnImage(),
                    icon: Icon(Icons.image_search),
                  ),
                ],
              ),
              SizedBox(height: 16),
              Row(
                children: [
                  FilledButton.icon(
                    onPressed: () {
                      Navigator.pop(context);
                    },
                    label: Text('Cancel'),
                    style: FilledButton.styleFrom(backgroundColor: Colors.red),
                  ),
                  SizedBox(
                    width: 16,
                  ),
                  FilledButton.icon(
                    onPressed: _submitData,
                    label: Text('Save'),
                    style: FilledButton.styleFrom(
                      backgroundColor: darkBlue,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
