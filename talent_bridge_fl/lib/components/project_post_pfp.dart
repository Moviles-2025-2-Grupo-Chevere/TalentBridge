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
    final sharedPreferences =
        await SharedPreferences.getInstance(); //Key-value DB for light data
    final storage = FirebaseStorage.instance;
    final localUrl = sharedPreferences.getString(cacheKey);

    if (mounted) {
      //mounted is used to only call setState if the widget is still in the widget tree
      setState(() {
        url = localUrl; // Uses cached URL first
      });
    }

    try {
      final remoteUrl = await storage
          .ref()
          .child('profile_pictures/$cacheKey')
          .getDownloadURL()
          .then((value) {
            sharedPreferences.setString(cacheKey, value);
            return value;
          })
          .timeout(Duration(seconds: 10));
      if (mounted) {
        setState(() {
          url = remoteUrl;
        });
      }
    } catch (e) {
      print(e); // Silently fails, keeps using cached URL
    }
  }

  @override
  void initState() {
    super.initState();
    cacheKey = widget.uid;
    setImageUrl();
  }

  @override
  Widget build(BuildContext context) {
    return CircleAvatar(
      radius: 24,
      backgroundImage: url != null
          ? CachedNetworkImageProvider(
              url!,
              cacheKey: cacheKey,
            )
          : AssetImage('assets/images/gumball.jpg') as ImageProvider,
    );
  }
}
