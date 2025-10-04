import 'dart:io';
import 'package:flutter/material.dart';

/// A reusable circular profile image widget
///
/// This widget handles both network and asset images with error fallback
/// and can be easily customized with different sizes and border properties
class CircularImageWidget extends StatelessWidget {
  final String? imageUrl;
  final double size;
  final Color backgroundColor;
  final Color borderColor;
  final double borderWidth;
  final Widget? placeholderWidget;
  final VoidCallback? onTap;

  const CircularImageWidget({
    super.key,
    this.imageUrl,
    this.size = 120.0,
    this.backgroundColor = const Color(0xFFE6E0F8), // Light purple background
    this.borderColor = Colors.transparent,
    this.borderWidth = 0.0,
    this.placeholderWidget,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          color: backgroundColor,
          shape: BoxShape.circle,
          border: Border.all(
            color: borderColor,
            width: borderWidth,
          ),
        ),
        child: ClipOval(
          child: imageUrl == null || imageUrl!.isEmpty
              ? placeholderWidget ??
                    Icon(
                      Icons.person,
                      size: size * 0.6,
                      color: Colors.black54,
                    )
              : imageUrl!.startsWith('http') || imageUrl!.startsWith('https')
              ? Image.network(
                  imageUrl!,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) {
                    return placeholderWidget ??
                        Icon(
                          Icons.person,
                          size: size * 0.6,
                          color: Colors.black54,
                        );
                  },
                )
              : imageUrl!.startsWith('/') || imageUrl!.contains('\\')
              ? Image.file(
                  File(imageUrl!),
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) {
                    return placeholderWidget ??
                        Icon(
                          Icons.person,
                          size: size * 0.6,
                          color: Colors.black54,
                        );
                  },
                )
              : Image.asset(
                  imageUrl!,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) {
                    return placeholderWidget ??
                        Icon(
                          Icons.person,
                          size: size * 0.6,
                          color: Colors.black54,
                        );
                  },
                ),
        ),
      ),
    );
  }
}
