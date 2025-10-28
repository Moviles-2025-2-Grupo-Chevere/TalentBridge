import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

/// Uses Cached network image to display the image of a project
class ProjectPostImage extends StatelessWidget {
  const ProjectPostImage({
    super.key,
    required this.imageUrlFuture,
    required this.imageKey,
  });

  final Future<String?>? imageUrlFuture;
  final String imageKey;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: imageUrlFuture,
      builder: (context, snapshot) {
        if (!snapshot.hasError) {
          return Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: 16,
              vertical: 8,
            ),
            child: ConstrainedBox(
              constraints: BoxConstraints(maxHeight: 300),
              child: CachedNetworkImage(
                cacheKey: imageKey,
                imageUrl: snapshot.data ?? '',
                placeholder: (context, url) => CircularProgressIndicator(),
                errorWidget: (context, url, error) => const SizedBox.shrink(),
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
