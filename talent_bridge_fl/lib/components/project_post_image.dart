import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

/// Uses Cached network image to display the image of a project
class ProjectPostImage extends StatelessWidget {
  const ProjectPostImage({
    super.key,
    required this.imageUrlFuture,
  });

  final Future<String?>? imageUrlFuture;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: imageUrlFuture,
      builder: (context, snapshot) {
        if (snapshot.hasData) {
          final imageUrl = snapshot.data;
          return Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: 16,
              vertical: 8,
            ),
            child: ConstrainedBox(
              constraints: BoxConstraints(maxHeight: 300),
              child: CachedNetworkImage(
                imageUrl: imageUrl!,
                placeholder: (context, url) => CircularProgressIndicator(),
                errorWidget: (context, url, error) => Icon(Icons.error),
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
