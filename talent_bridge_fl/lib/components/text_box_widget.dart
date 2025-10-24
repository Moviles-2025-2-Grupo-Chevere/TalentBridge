import 'package:flutter/material.dart';

/// A reusable widget for displaying text in a yellow-bordered box
///
/// Commonly used for skills, tags, or 'flags' in a profile
/// This widget can be easily customized with different border colors,
/// text styles, and padding
class TextBoxWidget extends StatelessWidget {
  final String text;
  final Color borderColor;
  final Color textColor;
  final EdgeInsetsGeometry padding;
  final double borderWidth;
  final double borderRadius;
  final VoidCallback? onTap;

  const TextBoxWidget({
    super.key,
    required this.text,
    this.borderColor = const Color(0xFF3E6990),
    this.textColor = const Color(0xFF3E6990),
    this.padding = const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
    this.borderWidth = 1.0,
    this.borderRadius = 4.0,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: padding,
        decoration: BoxDecoration(
          border: Border.all(
            color: borderColor,
            width: borderWidth,
          ),
          borderRadius: BorderRadius.circular(borderRadius),
        ),
        child: Text(
          text,
          style: TextStyle(
            color: textColor,
            fontWeight: FontWeight.w500,
          ),
        ),
      ),
    );
  }
}
