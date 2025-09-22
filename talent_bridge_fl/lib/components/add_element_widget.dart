// This is the widget for adding stuff like CV
import 'package:flutter/material.dart';

/// A reusable widget for displaying a link container with a title,
/// add icon, and an underlined "Add link" text
///
/// This widget uses a column layout for easy stacking of elements and
/// provides customization for colors, text styles, and callbacks
class AddElementWidget extends StatelessWidget {
  final String title;
  final VoidCallback onTap;
  final String linkText;
  final Color titleColor;
  final Color iconColor;
  final Color linkTextColor;
  final double iconSize;

  const AddElementWidget({
    super.key,
    required this.title,
    required this.onTap,
    this.linkText = 'Agregar link',
    this.titleColor = const Color(0xFFFFD700), // Yellow color for title
    this.iconColor = const Color(0xFF888888), // Gray color for icon
    this.linkTextColor = Colors.blue,
    this.iconSize = 40.0,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Title text
        Text(
          title,
          style: TextStyle(
            color: titleColor,
            fontSize: 18.0,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 8.0),
        // Plus icon container
        InkWell(
          onTap: onTap,
          child: Container(
            width: 80.0,
            height: 80.0,
            decoration: BoxDecoration(
              color: Colors.grey.shade200,
              borderRadius: BorderRadius.circular(10.0),
            ),
            child: Center(
              child: Icon(
                Icons.add,
                color: iconColor,
                size: iconSize,
              ),
            ),
          ),
        ),
        const SizedBox(height: 8.0),
        // "Agregar link" text
        InkWell(
          onTap: onTap,
          child: Text(
            linkText,
            style: TextStyle(
              color: linkTextColor,
              decoration: TextDecoration.underline,
              fontSize: 14.0,
            ),
          ),
        ),
      ],
    );
  }
}
