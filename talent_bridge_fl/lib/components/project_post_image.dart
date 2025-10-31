import 'package:cached_network_image/cached_network_image.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Uses Cached network image to display the image of a project
class ProjectPostImage extends StatefulWidget {
  const ProjectPostImage({
    super.key,
    required this.project,
  });

  final ProjectEntity project;

  @override
  State<ProjectPostImage> createState() => _ProjectPostImageState();
}

class _ProjectPostImageState extends State<ProjectPostImage> {
  String? imageUrl;
  late String imgKey;

  Future setImageUrl() async {
    final shared = await SharedPreferences.getInstance();
    final storage = FirebaseStorage.instance;
    final localUrl = shared.getString(imgKey);
    if (mounted) {
      setState(() {
        imageUrl = localUrl;
      });
    }
    try {
      final remoteUrl = await storage
          .ref()
          .child('project_images/$imgKey')
          .getDownloadURL()
          .then(
            (value) {
              shared.setString(imgKey, value);
              return value;
            },
          )
          .timeout(Duration(seconds: 10));
      if (mounted) {
        setState(() {
          imageUrl = remoteUrl;
        });
      }
    } catch (e) {
      print(e);
    }
  }

  @override
  void initState() {
    super.initState();
    imgKey = widget.project.id!;
    setImageUrl();
  }

  @override
  Widget build(BuildContext context) {
    return imageUrl == null
        ? SizedBox.shrink()
        : Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: ConstrainedBox(
              constraints: BoxConstraints(maxHeight: 300),
              child: CachedNetworkImage(
                imageUrl: imageUrl!,
                cacheKey: imgKey,
                errorWidget: (context, url, error) => Center(
                  child: Icon(Icons.error),
                ),
                placeholder: (context, url) => CircularProgressIndicator(),
              ),
            ),
          );
  }
}
