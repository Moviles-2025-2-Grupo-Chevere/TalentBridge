import 'package:flutter/material.dart';

class ContactItem extends StatelessWidget {
  const ContactItem({
    super.key,
    required this.label,
    this.value,
    this.fallback = '',
    this.fallbackAction,
  });

  final String label;
  final String? value;
  final String fallback;
  final VoidCallback? fallbackAction;

  @override
  Widget build(BuildContext context) {
    var displayFallback = false;
    if ((value ?? '').isEmpty) displayFallback = true;
    return Padding(
      padding: const EdgeInsets.only(bottom: 16.0),
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              label,
              style: const TextStyle(
                color: Colors.green,
                fontSize: 14.0,
                fontWeight: FontWeight.w500,
              ),
            ),
            displayFallback
                ? TextButton(
                    onPressed: fallbackAction ?? () {},
                    child: Text(
                      fallback,
                      style: const TextStyle(color: Colors.blue),
                    ),
                  )
                : Text(
                    value!,
                    style: const TextStyle(
                      fontSize: 14.0,
                    ),
                  ),
          ],
        ),
      ),
    );
  }
}
