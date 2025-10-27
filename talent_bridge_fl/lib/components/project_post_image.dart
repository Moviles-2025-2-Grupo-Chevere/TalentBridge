import 'package:flutter/material.dart';

class ProjectPostImage extends StatelessWidget {
  const ProjectPostImage({
    super.key,
    required this.imageUrl,
  });

  final Future<String?>? imageUrl;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: imageUrl,
      builder: (context, snapshot) {
        if (snapshot.hasData) {
          return Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: 16,
              vertical: 8,
            ),
            child: ConstrainedBox(
              constraints: BoxConstraints(maxHeight: 300),
              child: Image.network(
                snapshot.data!,
                fit: BoxFit.contain,
              ),
            ),
          );
        }
        return const SizedBox.shrink();
      },
    );
  }
}
