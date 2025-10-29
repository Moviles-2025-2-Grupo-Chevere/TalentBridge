import 'package:cached_network_image/cached_network_image.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';

class SkillImage extends StatelessWidget {
  const SkillImage({super.key, required this.icon});

  final String icon;

  @override
  Widget build(BuildContext context) {
    final future = FirebaseStorage.instance
        .ref()
        .child('skill_icons/$icon.png')
        .getDownloadURL();
    return FutureBuilder(
      future: future,
      builder: (context, snapshot) {
        if (snapshot.hasData) {
          return CachedNetworkImage(
            imageUrl: snapshot.data!,
          );
        }
        return SizedBox.shrink();
      },
    );
  }
}
