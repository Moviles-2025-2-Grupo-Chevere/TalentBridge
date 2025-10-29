import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ProjectPostPfp extends StatefulWidget {
  const ProjectPostPfp({super.key, required this.uid});

  final String uid;
  @override
  State<ProjectPostPfp> createState() => _ProjectPostPfpState();
}

class _ProjectPostPfpState extends State<ProjectPostPfp> {
  String? url;
  late String cacheKey;

  Future<void> setImageUrl() async {
    final shared_preferences =
        await SharedPreferences.getInstance(); //Key-value DB for light data
  }

  final storageRef = FirebaseStorage.instance.ref();
  @override
  void initState() {
    super.initState();
    final imageRef = storageRef.child('profile_pictures/${widget.uid}');
    imageRef.getDownloadURL().then(
      (value) {
        if (mounted) {
          setState(() {
            url = value;
          });
        }
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return CircleAvatar(
      radius: 24,
      backgroundImage: url != null
          ? NetworkImage(url!)
          : AssetImage('assets/images/gumball.jpg'),
    );
  }
}
