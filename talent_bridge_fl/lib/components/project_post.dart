import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/views/user-profile/user_profile.dart';
import 'package:cached_network_image/cached_network_image.dart'; // <-- NEW

class ProjectPost extends StatelessWidget {
  const ProjectPost({
    super.key,
    required this.project,
    required this.showApplyModal,
  });

  final ProjectEntity project;
  final void Function(String, String) showApplyModal;

  bool _isNetwork(String? path) {
    if (path == null) return false;
    final p = path.toLowerCase();
    return p.startsWith('http://') || p.startsWith('https://');
  }

  @override
  Widget build(BuildContext context) {
    final profilePictureUrl = project.createdBy?.photoUrl;
    final _firebaseService = FirebaseService();

    // --- Avatar provider: use disk cache if it's a URL; keep asset fallback ---
    ImageProvider? _avatarProvider() {
      if (profilePictureUrl == null || profilePictureUrl!.isEmpty) {
        return const AssetImage('assets/images/gumball.jpg');
      }
      if (_isNetwork(profilePictureUrl)) {
        return CachedNetworkImageProvider(profilePictureUrl!);
      }
      return AssetImage(profilePictureUrl!);
    }

    // --- Project image: use cache for network images; assets otherwise ---
    Widget? _postImage() {
      final url = project.imgUrl;
      if (url == null || url.isEmpty) return null;
      if (_isNetwork(url)) {
        return Image(image: CachedNetworkImageProvider(url), fit: BoxFit.cover);
      }
      return Image.asset(url);
    }

    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 8),
        child: Column(
          children: [
            // Header with avatar and info
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(
                    vertical: 4,
                    horizontal: 2,
                  ),
                  child: InkWell(
                    splashColor: Colors.white,
                    onTap: () {
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (ctx) => Scaffold(
                            appBar: AppBar(),
                            body: const UserProfile(),
                          ),
                        ),
                      );
                    },
                    customBorder: const CircleBorder(),
                    child: CircleAvatar(
                      radius: 24,
                      backgroundImage: _avatarProvider(),
                    ),
                  ),
                ),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        project.title,
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                      Text(
                        "${project.createdBy?.displayName ?? 'Project Manager'} · 5m",
                        style: const TextStyle(fontWeight: FontWeight.w300),
                      ),
                      Text(project.description),
                    ],
                  ),
                ),
              ],
            ),

            // Project image (asset or cached network)
            if (_postImage() != null) _postImage()!,

            // Skills
            Wrap(
              spacing: 4,
              children: [
                ...project.skills.map(
                  (v) => OutlinedButton(onPressed: () {}, child: Text(v)),
                ),
              ],
            ),

            // Action buttons
            Wrap(
              spacing: 4,
              children: [
                TextButton(onPressed: () {}, child: const Text('Comments')),
                TextButton(onPressed: () {}, child: const Text('Save')),
                TextButton(
                  onPressed: () {
                    final currentUserId = _firebaseService.currentUid() ?? "";
                    showApplyModal(currentUserId, project.id ?? "");
                  },
                  child: const Text('Apply'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
