import 'package:flutter/material.dart';

class SubmitAlert extends StatelessWidget {
  SubmitAlert({
    super.key,
  });

  final elevatedButtonStyle = ElevatedButton.styleFrom(
    minimumSize: Size(64, 32), // width, height
    padding: EdgeInsets.symmetric(
      horizontal: 4,
      vertical: 8,
    ),
    textStyle: TextStyle(fontSize: 14),
  );

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Expanded(
        child: Text(
          'Application Submitted!',
          textAlign: TextAlign.center,
        ),
      ),
      content: Expanded(
        child: Text(
          'Your application has been submitted successfully.',
          textAlign: TextAlign.center,
        ),
      ),
      actions: [
        ElevatedButton(
          style: elevatedButtonStyle,
          onPressed: () {
            Navigator.of(context).pop();
          },
          child: Text('My Applications'),
        ),
        ElevatedButton(
          style: elevatedButtonStyle,
          onPressed: () {
            Navigator.of(context).pop();
          },
          child: Text('Keep Exploring'),
        ),
      ],
    );
  }
}
