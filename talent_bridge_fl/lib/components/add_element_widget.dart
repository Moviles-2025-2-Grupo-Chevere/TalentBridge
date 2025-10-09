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
    this.titleColor = const Color(0xFF3E6990), // Yellow color for title
    this.iconColor = const Color(0xFF888888), // Gray color for icon
    this.linkTextColor = Colors.blue,
    this.iconSize = 40.0,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.center,
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
        SquareAddButton(onTap: onTap),
        const SizedBox(height: 8.0),
        Text(
          linkText,
          style: TextStyle(
            color: linkTextColor,
            fontSize: 14.0,
          ),
        ),
      ],
    );
  }
}

class SquareAddButton extends StatelessWidget {
  const SquareAddButton({
    super.key,
    required this.onTap,
  });

  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return IconButton.filled(
      onPressed: onTap,
      icon: Icon(Icons.add),
      style: ButtonStyle(
        shape: WidgetStateProperty.all<RoundedRectangleBorder>(
          RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
      ),
    );
  }
}
