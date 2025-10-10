import 'package:flutter/material.dart';

class SubmitAlertDb extends StatelessWidget {
  final String userId;
  final String projectId;
  final Function onConfirm;

  const SubmitAlertDb({
    Key? key,
    required this.userId,
    required this.projectId,
    required this.onConfirm,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('Apply to Project'),
      content: Text('Are you sure you want to apply to this project?'),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: Text('Cancel'),
        ),
        ElevatedButton(
          onPressed: () => onConfirm(),
          child: Text('Apply'),
        ),
      ],
    );
  }
}
