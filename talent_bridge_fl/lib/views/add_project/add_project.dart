import 'dart:collection';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:talent_bridge_fl/data/project_service.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/domain/skill_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/services/skills_service.dart';
import 'package:talent_bridge_fl/views/select_skills/select_skills.dart';

const darkBlue = Color(0xFF3E6990);

class AddProject extends StatefulWidget {
  const AddProject({super.key, required this.onAddProject});

  final void Function(ProjectEntity project, String? imagePath) onAddProject;

  @override
  State<AddProject> createState() => _AddProjectState();
}

class _AddProjectState extends State<AddProject> {
  final picker = ImagePicker();
  final firebaseService = FirebaseService();
  final projectService = ProjectService();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  // final _skillsController = TextEditingController();
  var _skills = SkillsService.getFallbackSkills();
  final _selectedSkills = HashSet<SkillEntity>();
  String? _imagePath;

  void _submitData() {
    final title = _titleController.text;
    final description = _descriptionController.text;
    final List<SkillEntity> skills = _selectedSkills.toList();
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
        skills: skills
            .map(
              (e) => e.label,
            )
            .toList(),
      ),
      _imagePath,
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

  void _removeSelectedSkill(SkillEntity skill) {
    setState(() {
      _selectedSkills.remove(skill);
    });
  }

  Future<void> _getAnImage() async {
    final XFile? image = await picker.pickImage(source: ImageSource.gallery);

    if (image != null) {
      print('Picked file path: ${image.path}');
      // You can convert it to File if needed:
      setState(() {
        _imagePath = image.path;
      });
    }
  }

  @override
  void initState() {
    super.initState();
    SkillsService.getRemoteSkills().then(
      (value) {
        if (mounted) {
          setState(() {
            _skills = value;
          });
        }
      },
    );
  }

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
                        label: Text(e.label),
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
              if (_imagePath != null) ...[
                SizedBox(
                  height: 8,
                ),
                ConstrainedBox(
                  constraints: BoxConstraints(maxHeight: 200),
                  child: Image.file(
                    File(_imagePath!),
                    fit: BoxFit.contain,
                  ),
                ),
              ],
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
