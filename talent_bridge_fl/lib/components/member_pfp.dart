import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:shared_preferences/shared_preferences.dart';

class MemberProfilePicture extends StatefulWidget {
  const MemberProfilePicture({
    super.key,
    required this.memberId,
    this.radius = 40.0,
  });

  final String memberId;
  final double radius;

  @override
  State<MemberProfilePicture> createState() => _MemberProfilePictureState();
}

class _MemberProfilePictureState extends State<MemberProfilePicture> {
  String? url;
  late String cacheKey;

  Future<void> setImageUrl() async {
    final sharedPreferences = await SharedPreferences.getInstance();
    final storage = FirebaseStorage.instance;
    final localUrl = sharedPreferences.getString(cacheKey);

    if (mounted) {
      setState(() {
        url = localUrl; // Uses cached URL first
      });
    }

    try {
      final remoteUrl = await storage
          .ref()
          .child('credits_pictures/$cacheKey.jpg')
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
      debugPrint('Error loading member picture: $e');
      // Silently fails, keeps using cached URL or shows placeholder
    }
  }

  @override
  void initState() {
    super.initState();
    cacheKey = widget.memberId;
    setImageUrl();
  }

  @override
  Widget build(BuildContext context) {
    return CircleAvatar(
      radius: widget.radius,
      backgroundColor: const Color(0xFF568C73).withOpacity(0.2),
      backgroundImage: url != null
          ? CachedNetworkImageProvider(
              url!,
              cacheKey: cacheKey,
            )
          : null,
      child: url == null
          ? Icon(
              Icons.person,
              size: widget.radius,
              color: const Color(0xFF568C73),
            )
          : null,
    );
  }
}
