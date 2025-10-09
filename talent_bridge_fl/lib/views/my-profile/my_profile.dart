import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:talent_bridge_fl/components/add_element_widget.dart';
import 'package:talent_bridge_fl/components/yellow_text_box_widget.dart';
import 'package:talent_bridge_fl/components/circular_image_widget.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/services/profile_pic_storage.dart';

const darkBlue = Color(0xFF3E6990);

class MyProfile extends StatefulWidget {
  const MyProfile({super.key});

  @override
  State<MyProfile> createState() => _MyProfileState();
}

class _MyProfileState extends State<MyProfile> {
  String? _profileImagePath;
  UserEntity? userEntity;
  final fb = FirebaseService();

  @override
  void initState() {
    super.initState();
    getPfP();
    getUserDocument();
  }

  Future<void> getUserDocument() async {
    var user = await fb.getCurrentUserEntity();
    setState(() {
      userEntity = user;
    });
  }

  void getPfP() {
    fb
        .getPFPUrl()
        .then((pfpUrl) {
          setState(() {
            _profileImagePath = pfpUrl;
          });
          print('Obtained Image Url');
        })
        .catchError((e) {
          _profileImagePath = null;
        });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      color: const Color.fromARGB(255, 255, 255, 255), // White background
      child: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Profile header with image and username
              Center(
                child: Column(
                  children: [
                    // Profile image
                    CircularImageWidget(
                      imageUrl: _profileImagePath,
                      size: 120.0,
                      onTap: _showTakePhotoDialog,
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
              const SizedBox(height: 24.0),

              // Contact section
              const Text(
                'Contact',
                style: TextStyle(
                  color: darkBlue,
                  fontSize: 18.0,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8.0),
              Center(
                child: Column(
                  children: [
                    ContactItem(label: 'Email', value: userEntity?.email ?? ''),
                    ContactItem(
                      label: 'Linkedin',
                      value: userEntity?.linkedin,
                      fallback: 'Add linkedin',
                    ),
                    ContactItem(
                      label: 'Mobile Number',
                      value: userEntity?.mobileNumber,
                      fallback: 'Add mobile number',
                    ),
                    ContactItem(
                      label: 'Major',
                      value: userEntity?.major,
                      fallback: 'Add major',
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24.0),

              // Description section
              const Text(
                'Your Description',
                style: TextStyle(
                  color: darkBlue,
                  fontSize: 18.0,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8.0),
              (userEntity?.description ?? '').isNotEmpty
                  ? Text(userEntity!.description!)
                  : Center(
                      child: TextButton(
                        onPressed: () {},
                        child: Text(
                          'Add description',
                          style: TextStyle(
                            color: Colors.blue,
                          ),
                        ),
                      ),
                    ),
              const SizedBox(height: 8.0),

              const Text(
                'Mis Flags',
                style: TextStyle(
                  color: darkBlue,
                  fontSize: 18.0,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8.0),
              Wrap(
                spacing: 8.0,
                runSpacing: 8.0,
                children: (userEntity?.skillsOrTopics ?? [])
                    .map((i) => YellowTextBoxWidget(text: i))
                    .toList(),
              ),
              Center(
                child: TextButton(
                  onPressed: () {},
                  child: const Text(
                    'Agregar flag',
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
              const Text(
                'Mis Proyectos',
                style: TextStyle(
                  color: darkBlue,
                  fontSize: 18.0,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8.0),
              Center(
                child: Column(
                  children: [
                    if ((userEntity?.projects ?? []).isEmpty)
                      Text(
                        'No tienes proyectos activos.',
                        style: TextStyle(
                          fontSize: 14.0,
                          color: Colors.grey[600],
                        ),
                      ),
                    const SizedBox(height: 16.0),
                    SquareAddButton(onTap: () {}),
                  ],
                ),
              ),
              const SizedBox(height: 40.0),
            ],
          ),
        ),
      ),
    );
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

      if (image != null && mounted) {
        fb
            .uploadPFP(File(image.path))
            .then((sn) {
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Profile picture uploaded!')),
                );
              }
            })
            .catchError((_) {
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text('Error uploading profile picture :('),
                  ),
                );
              }
            });

        setState(() {
          _profileImagePath = image.path;
        });
        // Save to storage
        ProfileStorage.saveProfileImagePath(image.path);
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
}

class ContactItem extends StatelessWidget {
  const ContactItem({
    super.key,
    required this.label,
    this.value,
    this.fallback = '',
    this.isLink = false,
    this.linkColor,
  });

  final String label;
  final String? value;
  final String fallback;
  final bool isLink;
  final Color? linkColor;

  @override
  Widget build(BuildContext context) {
    bool displayLink = isLink;
    if ((value ?? '').isEmpty) displayLink = true;
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              label,
              style: const TextStyle(
                color: Colors.green,
                fontSize: 14.0,
                fontWeight: FontWeight.w500,
              ),
            ),
            const SizedBox(height: 4.0),
            displayLink
                ? InkWell(
                    onTap: () {
                      // Handle link tap
                    },
                    child: Text(
                      fallback,
                      style: TextStyle(
                        color: linkColor ?? Colors.blue,
                        decoration: TextDecoration.underline,
                        fontSize: 14.0,
                      ),
                    ),
                  )
                : Text(
                    value!,
                    style: const TextStyle(fontSize: 14.0),
                  ),
          ],
        ),
      ),
    );
  }
}
