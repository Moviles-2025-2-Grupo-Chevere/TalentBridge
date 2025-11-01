import 'package:flutter/material.dart';

class SubmitAlertDb extends StatefulWidget {
  final String userId;
  final String projectId;
  final Future<void> Function() onConfirm;

  const SubmitAlertDb({
    super.key,
    required this.userId,
    required this.projectId,
    required this.onConfirm,
  });

  @override
  State<SubmitAlertDb> createState() => _SubmitAlertDbState();
}

class _SubmitAlertDbState extends State<SubmitAlertDb> {
  bool _isLoading = false;

  Future<void> _handleConfirm() async {
    setState(() {
      _isLoading = true;
    });

    try {
      await widget.onConfirm();
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('Apply to Project'),
      content: _isLoading
          ? Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                CircularProgressIndicator(),
                SizedBox(height: 16),
                Text('Submitting application...'),
              ],
            )
          : Text('Are you sure you want to apply to this project?'),
      actions: [
        TextButton(
          onPressed: _isLoading ? null : () => Navigator.of(context).pop(),
          child: Text('Cancel'),
        ),
        ElevatedButton(
          onPressed: _isLoading ? null : _handleConfirm,
          child: _isLoading
              ? SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                  ),
                )
              : Text('Apply'),
        ),
      ],
    );
  }
}
