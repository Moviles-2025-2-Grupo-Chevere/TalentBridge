import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:talent_bridge_fl/components/add_element_widget.dart';
import 'package:talent_bridge_fl/components/text_box_widget.dart';
import 'package:talent_bridge_fl/data/project_service.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/domain/update_user_dto.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/providers/profile_provider.dart';
import 'package:talent_bridge_fl/providers/upload_queue.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/services/profile_pic_storage.dart';
import 'package:talent_bridge_fl/views/add_project/add_project.dart';
import 'package:talent_bridge_fl/views/edit_profile/edit_profile.dart';
import 'package:talent_bridge_fl/views/my-profile/contact_item.dart';
import 'package:talent_bridge_fl/views/my-profile/project_summary.dart';

const darkBlue = Color(0xFF3E6990);

const headerStyle = TextStyle(
  color: darkBlue,
  fontSize: 18.0,
  fontWeight: FontWeight.bold,
);

class MyProfile extends ConsumerStatefulWidget {
  const MyProfile({super.key});

  @override
  ConsumerState<MyProfile> createState() => _MyProfileState();
}

class _MyProfileState extends ConsumerState<MyProfile> {
  ImageProvider? pfpProvider;
  bool syncingImage = false;
  final fb = FirebaseService();
  final projectService = ProjectService();

  @override
  void initState() {
    super.initState();
    getPfP();
  }

  /// Uses offline first, online fallback for getting the profile picture.
  Future<void> getPfP() async {
    final localPath = await ProfileStorage.getLocalProfileImagePath();
    if (localPath != null) {
      setState(() {
        pfpProvider = FileImage(File(localPath));
      });
      return;
    }
    try {
      final remotePfpUrl = await fb.getPFPUrl();
      if (remotePfpUrl != null) {
        setState(() {
          pfpProvider = NetworkImage(remotePfpUrl);
        });
        debugPrint('Obtained Image Url from network');
      }
    } catch (e) {
      debugPrint(e.toString());
    }
  }

  // Show dialog to confirm taking a profile picture
  void _showTakePhotoDialog() {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Take Profile Picture'),
          content: const Text('Do you want to take a new profile picture?'),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop(); // Cancel
              },
              child: const Text('Cancel'),
            ),
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
                _takePhoto(); // Accept
              },
              child: const Text('Accept'),
            ),
          ],
        );
      },
    );
  }

  // Take photo using system camera
  Future<void> _takePhoto() async {
    try {
      final ImagePicker picker = ImagePicker();

      // Use the device's camera app
      final XFile? image = await picker.pickImage(
        source: ImageSource.camera,
        imageQuality: 85, // Compress to 85% quality
        preferredCameraDevice: CameraDevice.front, // Start with front camera
      );

      if (image != null) {
        final localPicturePath = await ProfileStorage.saveProfilePictureLocally(
          image.path,
        );
        try {
          final result = await ref
              .read(pfpUploadProvider.notifier)
              .enqueuePfpUpload(localPicturePath);
          if (result == null && mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text('The picture will be uploaded later'),
              ),
            );
          }
        } catch (e) {
          debugPrint(e.toString());
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text('Error uploading profile picture :('),
              ),
            );
          }
        }
        final evicted = await (pfpProvider as FileImage).evict();
        debugPrint('Evicted: $evicted');
        setState(() {
          debugPrint("Set Image path to: $localPicturePath");
          pfpProvider = FileImage(File(image.path));
        });
        // Save to storage
      }
    } catch (e) {
      _showErrorDialog('Error accessing camera: $e');
    }
  }

  // Show error dialog
  void _showErrorDialog(String message) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Error'),
          content: Text(message),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              child: const Text('OK'),
            ),
          ],
        );
      },
    );
  }

  void _onSubmitProject(ProjectEntity project) {
    projectService.createProject(project);
  }

  void _openAddProjectOverlay() {
    showModalBottomSheet(
      useSafeArea: true,
      isScrollControlled: true,
      context: context,
      builder: (_) => AddProject(
        onAddProject: _onSubmitProject,
      ),
    );
  }

  void _openEditProfileOverlay(UserEntity user) {
    showModalBottomSheet(
      useSafeArea: true,
      isScrollControlled: true,
      context: context,
      builder: (context) => EditProfile(
        onUpdate: (updateDto) {},
        existingData: UpdateUserDto(
          displayName: user.displayName,
          headline: user.headline ?? '',
          linkedin: user.linkedin ?? '',
          mobileNumber: user.mobileNumber ?? '',
          skillsOrTopics: user.skillsOrTopics ?? [],
          description: user.description ?? '',
          major: user.major ?? '',
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    ref.listen(
      pfpUploadProvider,
      (prev, next) {
        if (prev != null && next == null) {
          ScaffoldMessenger.of(
            context,
          ).showSnackBar(SnackBar(content: Text("Profile Picture uploaded")));
        }
      },
    );
    final pendingUpload = ref.watch(pfpUploadProvider) != null;
    var userEntity = ref.watch(profileProvider);

    return SingleChildScrollView(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Profile header with image and username
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Spacer(),
                SizedBox(
                  child: Column(
                    children: [
                      // Profile image
                      Stack(
                        children: [
                          InkWell(
                            onTap: _showTakePhotoDialog,
                            child: CircleAvatar(
                              radius: 60,
                              backgroundImage: pfpProvider,
                            ),
                          ),
                          if (pendingUpload)
                            Positioned(
                              bottom: 0,
                              right: 0,
                              child: Icon(Icons.sync_alt_outlined),
                            ),
                        ],
                      ),
                      const SizedBox(height: 16.0),
                      // Username
                      Text(
                        userEntity?.displayName ?? '',
                        style: TextStyle(
                          fontSize: 18.0,
                          fontWeight: FontWeight.bold,
                          fontFamily: 'OpenSans',
                        ),
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: Align(
                    alignment: Alignment.topRight,
                    child: IconButton(
                      onPressed: userEntity != null
                          ? () => _openEditProfileOverlay(userEntity!)
                          : () {},
                      icon: Icon(Icons.edit),
                    ),
                  ),
                ),
              ],
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                (userEntity?.headline ?? '').isEmpty
                    ? TextButton(
                        onPressed: () => _openEditProfileOverlay(userEntity!),
                        child: Text(
                          'Add headline',
                          style: const TextStyle(color: Colors.blue),
                        ),
                      )
                    : Expanded(
                        child: Text(
                          userEntity!.headline!,
                          textAlign: TextAlign.center,
                        ),
                      ),
              ],
            ),
            const SizedBox(height: 16.0),
            // Contact section
            const Text(
              'Contact',
              style: headerStyle,
            ),
            const SizedBox(height: 8.0),
            Column(
              children: [
                ContactItem(label: 'Email', value: userEntity?.email ?? ''),
                ContactItem(
                  label: 'Linkedin',
                  value: userEntity?.linkedin,
                  fallback: 'Add LinkedIn',
                  fallbackAction: () => _openEditProfileOverlay(userEntity!),
                ),
                ContactItem(
                  label: 'Mobile Number',
                  value: userEntity?.mobileNumber,
                  fallback: 'Add mobile number',
                  fallbackAction: () => _openEditProfileOverlay(userEntity!),
                ),
                ContactItem(
                  label: 'Major',
                  value: userEntity?.major,
                  fallback: 'Add major',
                  fallbackAction: () => _openEditProfileOverlay(userEntity!),
                ),
              ],
            ),

            // Description section
            const Text('Your Description', style: headerStyle),
            const SizedBox(height: 8.0),
            (userEntity?.description ?? '').isNotEmpty
                ? Text(userEntity!.description!)
                : Center(
                    child: TextButton(
                      onPressed: () => _openEditProfileOverlay(userEntity!),
                      child: Text(
                        'Add description',
                        style: TextStyle(
                          color: Colors.blue,
                        ),
                      ),
                    ),
                  ),
            const SizedBox(height: 8.0),

            const Text('My Skills and Topics', style: headerStyle),
            const SizedBox(height: 8.0),
            Wrap(
              spacing: 8.0,
              runSpacing: 8.0,
              children: (userEntity?.skillsOrTopics ?? [])
                  .map((i) => TextBoxWidget(text: i))
                  .toList(),
            ),
            Center(
              child: TextButton(
                onPressed: () => _openEditProfileOverlay(userEntity!),
                child: const Text(
                  'Add Skills',
                  style: TextStyle(
                    color: Colors.blue,
                  ),
                ),
              ),
            ),
            const SizedBox(height: 24.0),

            // Link sections for CV and Portfolio
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: AddElementWidget(
                    title: 'Agregar CV',
                    onTap: () {
                      // Add CV action
                    },
                  ),
                ),
                const SizedBox(width: 16.0),
                Expanded(
                  child: AddElementWidget(
                    title: 'Agregar Portafolio',
                    onTap: () {
                      // Add Portfolio action
                    },
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24.0),

            // Projects section
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('My Projects', style: headerStyle),
                Spacer(),
                SquareAddButton(onTap: _openAddProjectOverlay),
              ],
            ),
            if ((userEntity?.projects ?? []).isEmpty)
              Center(
                child: Column(
                  children: [
                    SizedBox(
                      height: 8,
                    ),
                    Text(
                      'No tienes proyectos activos.',
                      style: TextStyle(
                        fontSize: 14.0,
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
              ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: (userEntity?.projects ?? [])
                  .map(
                    (i) => ProjectSummary(
                      project: i,
                    ),
                  )
                  .toList(),
            ),
            const SizedBox(height: 40.0),
          ],
        ),
      ),
    );
  }
}
