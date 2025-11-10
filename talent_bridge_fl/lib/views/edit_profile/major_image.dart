import 'package:cached_network_image/cached_network_image.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';

class MajorImage extends StatelessWidget {
  const MajorImage({super.key, required this.icon});

  final String icon;

  @override
  Widget build(BuildContext context) {
    final storage = FirebaseStorage.instance;
    final remoteUrl = storage
        .ref()
        .child('major_icons/$icon.png')
        .getDownloadURL();
    return FutureBuilder(
      future: remoteUrl,
      builder: (context, snapshot) {
        return CachedNetworkImage(
          cacheKey: "major-$icon",
          imageUrl: snapshot.data ?? '',
          placeholder: (context, url) => const SizedBox(
            width: 24,
          ),
          errorWidget: (context, url, error) => const SizedBox(
            width: 24,
          ),
        );
      },
    );
  }
}
