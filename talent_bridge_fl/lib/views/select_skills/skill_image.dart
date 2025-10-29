import 'package:cached_network_image/cached_network_image.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SkillImage extends StatefulWidget {
  const SkillImage({super.key, required this.icon});

  final String icon;

  @override
  State<SkillImage> createState() => _SkillImageState();
}

class _SkillImageState extends State<SkillImage> {
  String? imageUrl;

  Future setImageUrl() async {
    final shared = await SharedPreferences.getInstance();
    final storage = FirebaseStorage.instance;
    final localUrl = shared.getString(widget.icon);
    if (mounted) {
      setState(() {
        imageUrl = localUrl;
      });
    }
    try {
      final remoteUrl = await storage
          .ref()
          .child('skill_icons/${widget.icon}.png')
          .getDownloadURL()
          .then(
            (value) {
              shared.setString(widget.icon, value);
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
    setImageUrl();
  }

  @override
  Widget build(BuildContext context) {
    return imageUrl == null
        ? SizedBox.shrink()
        : CachedNetworkImage(
            width: 24,
            fit: BoxFit.contain,
            imageUrl: imageUrl!,
            cacheKey: widget.icon,
            imageBuilder: (context, imageProvider) => Image(
              image: imageProvider,
              fit: BoxFit.contain,
              color: Colors.black,
              colorBlendMode: BlendMode.srcIn,
            ),
          );
  }
}
