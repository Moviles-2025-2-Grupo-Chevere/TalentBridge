import 'package:flutter/material.dart';

class AddProject extends StatefulWidget {
  const AddProject({super.key, required this.onAddProject});

  final void Function() onAddProject;

  @override
  State<AddProject> createState() => _AddProjectState();
}

class _AddProjectState extends State<AddProject> {
  void _submitData() {
    widget.onAddProject();
  }

  @override
  Widget build(BuildContext context) {
    return Placeholder();
  }
}
