import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:talent_bridge_fl/components/add_element_widget.dart';
import 'package:talent_bridge_fl/components/yellow_text_box_widget.dart';
import 'package:talent_bridge_fl/components/circular_image_widget.dart';
import 'package:talent_bridge_fl/data/project_service.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/services/profile_pic_storage.dart';
import 'package:talent_bridge_fl/views/add_project/add_project.dart';

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
  final projectService = ProjectService();

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
              Center(
                child: Column(
                  children: [
                    if ((userEntity?.projects ?? []).isEmpty)
                      Padding(
                        padding: const EdgeInsets.only(top: 8),
                        child: Text(
                          'No tienes proyectos activos.',
                          style: TextStyle(
                            fontSize: 14.0,
                            color: Colors.grey[600],
                          ),
                        ),
                      ),
                    const SizedBox(height: 16.0),
                    SquareAddButton(onTap: _openAddProjectOverlay),
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
      ),
    );
  }
}

class ProjectSummary extends StatelessWidget {
  const ProjectSummary({
    super.key,
    required this.project,
  });

  final ProjectEntity project;

  @override
  Widget build(BuildContext context) {
    final firebaseService = FirebaseService();

    return InkWell(
      onTap: () {
        // Show applicants modal when project is clicked
        showDialog(
          context: context,
          builder: (BuildContext context) {
            return Dialog(
              child: FutureBuilder<List<Map<String, String>>>(
                future: firebaseService.getUsersAppliedToProject(
                  projectId: project.id ?? '',
                ),
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return const Padding(
                      padding: EdgeInsets.all(20.0),
                      child: Center(child: CircularProgressIndicator()),
                    );
                  } else if (snapshot.hasError) {
                    return Padding(
                      padding: const EdgeInsets.all(20.0),
                      child: Text(
                        'Error loading applicants: ${snapshot.error}',
                      ),
                    );
                  } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                    return const Padding(
                      padding: EdgeInsets.all(20.0),
                      child: Text('No applicants found for this project.'),
                    );
                  }

                  return Container(
                    constraints: BoxConstraints(maxHeight: 400),
                    padding: EdgeInsets.all(16),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          'Applicants for ${project.title}',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 18,
                          ),
                          textAlign: TextAlign.center,
                        ),
                        SizedBox(height: 16),
                        Flexible(
                          child: ListView.builder(
                            shrinkWrap: true,
                            itemCount: snapshot.data!.length,
                            itemBuilder: (context, index) {
                              final applicant = snapshot.data![index];
                              return Card(
                                margin: EdgeInsets.only(bottom: 8),
                                child: Padding(
                                  padding: EdgeInsets.all(8),
                                  child: Row(
                                    children: [
                                      CircleAvatar(
                                        child: Text(
                                          applicant['name']?.substring(0, 1) ??
                                              '?',
                                        ),
                                      ),
                                      SizedBox(width: 12),
                                      Expanded(
                                        child: Text(
                                          applicant['name'] ?? 'Unknown',
                                        ),
                                      ),
                                      ElevatedButton(
                                        onPressed: () async {
                                          try {
                                            // Show loading indicator
                                            ScaffoldMessenger.of(
                                              context,
                                            ).showSnackBar(
                                              SnackBar(
                                                content: Text(
                                                  'Accepting application...',
                                                ),
                                              ),
                                            );

                                            // Call accept method with applicant's ID and project ID
                                            await firebaseService.acceptProject(
                                              userId: applicant['id'] ?? '',
                                              projectId: project.id ?? '',
                                            );

                                            // Show success message
                                            ScaffoldMessenger.of(
                                              context,
                                            ).showSnackBar(
                                              SnackBar(
                                                content: Text(
                                                  'Application accepted successfully!',
                                                ),
                                              ),
                                            );

                                            // Close the dialog
                                            Navigator.of(context).pop();
                                          } catch (e) {
                                            // Show error message
                                            ScaffoldMessenger.of(
                                              context,
                                            ).showSnackBar(
                                              SnackBar(
                                                content: Text(
                                                  'Error accepting application: $e',
                                                ),
                                              ),
                                            );
                                          }
                                        },
                                        style: ElevatedButton.styleFrom(
                                          backgroundColor: Colors.green,
                                        ),
                                        child: Text('A'),
                                      ),
                                      SizedBox(width: 8),
                                      ElevatedButton(
                                        onPressed: () {
                                          // Reject logic will go here
                                        },
                                        style: ElevatedButton.styleFrom(
                                          backgroundColor: Colors.red,
                                        ),
                                        child: Text('R'),
                                      ),
                                    ],
                                  ),
                                ),
                              );
                            },
                          ),
                        ),
                        SizedBox(height: 8),
                        TextButton(
                          onPressed: () => Navigator.of(context).pop(),
                          child: Text('Close'),
                        ),
                      ],
                    ),
                  );
                },
              ),
            );
          },
        );
      },
      child: Card(
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Column(
            children: [
              Row(
                children: [
                  Text(project.title),
                  Spacer(),
                  Icon(Icons.person),
                  SizedBox(width: 8),
                  Text('2'), // TODO remove hardcode
                ],
              ),
              SizedBox(height: 8),
              Wrap(
                crossAxisAlignment: WrapCrossAlignment.center,
                spacing: 4,
                children: [
                  ...project.skills
                      .sublist(
                        0,
                        project.skills.length > 2 ? 2 : project.skills.length,
                      )
                      .map(
                        (v) => OutlinedButton(onPressed: () {}, child: Text(v)),
                      ),
                  if (project.skills.length > 2) Text('...'),
                ],
              ),
            ],
          ),
        ),
      ),
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
