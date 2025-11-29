import 'dart:async';
import 'dart:io';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:firebase_storage/firebase_storage.dart' as firebase_core;
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
import 'package:talent_bridge_fl/providers/upload_queue_cv.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/services/profile_pic_storage.dart';
import 'package:talent_bridge_fl/views/add_portfolio/add_portfolio.dart';
import 'package:talent_bridge_fl/views/add_project/add_project.dart';
import 'package:file_picker/file_picker.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:talent_bridge_fl/views/edit_profile/edit_profile.dart';
import 'package:talent_bridge_fl/views/my-profile/contact_item.dart';
import 'package:talent_bridge_fl/views/my-profile/project_summary.dart';
import 'package:talent_bridge_fl/views/my-profile/download_cvs.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

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
  String? _bannerImageUrl;
  bool syncingImage = false;
  StreamSubscription<List<ConnectivityResult>>? _connectivitySubscription;
  bool _isOnline = true;
  final fb = FirebaseService();
  final projectService = ProjectService();

  @override
  void initState() {
    super.initState();
    getPfP();
    setBannerFromUrl(FirebaseStorage.instance, fb.currentUid()!);
    _checkConnectivity();

    // Listen to connectivity changes
    _connectivitySubscription = Connectivity().onConnectivityChanged.listen((
      results,
    ) {
      final isConnected = results.any(
        (result) =>
            result == ConnectivityResult.mobile ||
            result == ConnectivityResult.wifi ||
            result == ConnectivityResult.ethernet,
      );

      if (mounted) {
        setState(() {
          _isOnline = isConnected;
        });
      }
    });
  }

  Future<void> _checkConnectivity() async {
    final results = await Connectivity().checkConnectivity();
    final isConnected = results.any(
      (result) =>
          result == ConnectivityResult.mobile ||
          result == ConnectivityResult.wifi ||
          result == ConnectivityResult.ethernet,
    );

    if (mounted) {
      setState(() {
        _isOnline = isConnected;
      });
    }
  }

  @override
  void dispose() {
    _connectivitySubscription?.cancel();
    super.dispose();
  }

  /// Uses offline first, online fallback for getting the profile picture.
  Future<void> getPfP() async {
    final localPath = await ProfileStorage.getLocalProfileImagePath();
    if (localPath != null) {
      setState(() {
        pfpProvider = FileImage(File(localPath));
      });
      debugPrint('Obtained Image From Local Storage');
      return;
    }
    try {
      final remotePfpUrl = await fb.getPFPUrl();
      if (remotePfpUrl != null) {
        setState(() {
          pfpProvider = CachedNetworkImageProvider(remotePfpUrl);
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

  void _showPickBannerImageDialog(String uid, BuildContext outerContext) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Pick Banner Image'),
          content: const Text('Do you want to pick a new banner image?'),
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
                _pickBannerImage(uid).then(
                  (value) {
                    if (outerContext.mounted) {
                      ScaffoldMessenger.of(outerContext).showSnackBar(
                        SnackBar(content: Text('Banner Image uploaded')),
                      );
                    }
                  },
                ); // Accept
              },
              child: const Text('Accept'),
            ),
          ],
        );
      },
    );
  }

  Future _pickBannerImage(String uid) async {
    final picker = ImagePicker();
    final storage = FirebaseStorage.instance;
    final XFile? image = await picker.pickImage(source: ImageSource.gallery);

    if (image != null) {
      print('Picked file path: ${image.path}');
      var ref = storage.ref().child('banner_images/$uid');
      try {
        await ref.putFile(File(image.path));
        await setBannerFromUrl(storage, uid);
      } on firebase_core.FirebaseException catch (e) {
        print('Firebase error: ${e.code} - ${e.message}');
        if (e.code == 'retry-limit-exceeded') {
          print('No internet connection.');
        } else {
          rethrow;
        }
      } catch (e) {
        rethrow;
      }
    }
  }

  Future<void> setBannerFromUrl(
    firebase_core.FirebaseStorage storage,
    String uid,
  ) async {
    final downloadUrl = await storage
        .ref()
        .child('banner_images/$uid')
        .getDownloadURL();
    if (mounted) {
      setState(() {
        _bannerImageUrl = downloadUrl;
      });
    }
  }

  // Handle PDF selection and upload
  // Handle PDF selection and upload with queue system
  Future<void> _pickAndUploadCVs() async {
    try {
      print('Picking PDF files...');
      // Pick multiple PDF files
      FilePickerResult? result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['pdf'],
        allowMultiple: true,
      );

      if (result == null || result.files.isEmpty) {
        // User canceled the picker
        return;
      }

      // Convert PlatformFile objects to File objects
      final List<File> files = result.paths
          .where((path) => path != null)
          .map((path) => File(path!))
          .toList();

      if (files.isEmpty) {
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('No valid files selected')),
        );
        return;
      }

      // Show preparing message
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Preparing to upload ${files.length} CV(s)...')),
      );

      // Enqueue CV uploads
      final results = await ref
          .read(cvUploadProvider.notifier)
          .enqueueCVUploads(files);

      if (!mounted) return;

      if (results.isNotEmpty) {
        // Successfully uploaded immediately
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Successfully uploaded ${results.length} CV(s)'),
            backgroundColor: Colors.green,
          ),
        );

        // Update portfolio timestamp
        await fb.updateLastPortfolioUpdate();
      } else {
        // Queued for later upload
        final cvText = files.length == 1 ? 'CV' : '${files.length} CVs';
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              '$cvText will be uploaded once connectivity is regained',
            ),
            backgroundColor: Colors.orange,
            duration: const Duration(seconds: 4),
          ),
        );
      }
    } catch (e) {
      // Show error message
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error preparing CVs: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  // Get a list of all CV download URLs for the current user
  void _viewUploadedCVs() async {
    try {
      // Show loading dialog
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => const AlertDialog(
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              CircularProgressIndicator(),
              SizedBox(height: 16),
              Text('Loading your CVs...'),
            ],
          ),
        ),
      );

      // Get CV URLs
      final List<String> cvUrls = await fb.getCVUrls();

      // Close loading dialog
      if (!mounted) return;
      Navigator.of(context).pop();

      if (cvUrls.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('You have no CVs uploaded yet')),
        );
        return;
      }

      // Show list of CVs
      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Your CVs'),
          content: SizedBox(
            width: double.maxFinite,
            child: ListView.builder(
              shrinkWrap: true,
              itemCount: cvUrls.length,
              itemBuilder: (context, index) {
                return ListTile(
                  leading: const Icon(Icons.picture_as_pdf),
                  title: Text('CV ${index + 1}'),
                  trailing: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      IconButton(
                        icon: const Icon(Icons.open_in_new),
                        onPressed: () {
                          _openPdf(cvUrls[index]);
                        },
                      ),
                      IconButton(
                        icon: const Icon(Icons.delete),
                        onPressed: () {
                          _deletePdf(cvUrls[index], index);
                          Navigator.pop(context); // Close dialog after delete
                          _viewUploadedCVs(); // Refresh the list
                        },
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
                DownloadCVs.downloadAllCVs(context, cvUrls);
              },
              child: const Text('Download All'),
            ),
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
                _pickAndUploadCVs();
              },
              child: const Text('Upload More'),
            ),
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Close'),
            ),
          ],
        ),
      );
    } catch (e) {
      if (!mounted) return;
      Navigator.of(context).pop(); // Close loading dialog if open
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error loading CVs: $e')),
      );
    }
  }

  // Open PDF using URL launcher
  Future<void> _openPdf(String url) async {
    try {
      final Uri pdfUrl = Uri.parse(url);
      if (!await launchUrl(pdfUrl, mode: LaunchMode.externalApplication)) {
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Could not open PDF')),
        );
      }
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error opening PDF: $e')),
      );
    }
  }

  // Delete PDF from Firebase Storage
  Future<void> _deletePdf(String url, int index) async {
    try {
      // Extract the storage reference from the URL
      final ref = FirebaseStorage.instance.refFromURL(url);
      await ref.delete();

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('CV ${index + 1} deleted successfully')),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error deleting CV: $e')),
      );
    }
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
          // TODO mira esto deivid
          final result = await ref
              .read(pfpUploadProvider.notifier)
              .enqueuePfpUpload(
                localPicturePath,
              ); // <<<<<<<<- TODO mira esto deivid
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
        if (pfpProvider != null && pfpProvider is FileImage) {
          final evicted = await (pfpProvider as FileImage).evict();
          debugPrint('Evicted: $evicted');
        }
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

  void _onSubmitProject(ProjectEntity project, String? imagePath) {
    projectService.createProject(project, imagePath);
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

  void _openAddPortfolioOverlay() {
    showModalBottomSheet(
      context: context,
      useSafeArea: true,
      isScrollControlled: true,
      builder: (_) => AddPortfolio(),
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
    debugPrint("Connectivity status in MyProfile: $_isOnline");

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

    ref.listen(
      cvUploadProvider,
      (prev, next) {
        if (prev != null && prev.isNotEmpty && next.length < prev.length) {
          final uploadedCount = prev.length - next.length;
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                uploadedCount == 1
                    ? 'CV uploaded successfully'
                    : '$uploadedCount CVs uploaded successfully',
              ),
              backgroundColor: Colors.green,
            ),
          );
        }
      },
    );
    final pendingUpload = ref.watch(pfpUploadProvider) != null;
    final pendingCVUploads = ref.watch(cvUploadProvider).length;
    final userEntity = ref.watch(profileProvider);

    return SingleChildScrollView(
      child: Stack(
        children: [
          ConstrainedBox(
            constraints: BoxConstraints(maxHeight: 200),
            child: Material(
              color: Colors.transparent, // important!
              child: _bannerImageUrl == null
                  ? Container(
                      decoration: BoxDecoration(color: Colors.teal),
                      child: InkWell(
                        onTap: () =>
                            _showPickBannerImageDialog(userEntity!.id, context),
                        splashColor: Colors.blue.withValues(alpha: 0.3),
                        highlightColor: Colors.transparent,
                      ),
                    )
                  : Ink.image(
                      image: CachedNetworkImageProvider(
                        _bannerImageUrl!,
                      ),
                      fit: BoxFit.cover,
                      child: InkWell(
                        onTap: () =>
                            _showPickBannerImageDialog(userEntity!.id, context),
                        splashColor: Colors.blue.withValues(alpha: 0.3),
                        highlightColor: Colors.transparent,
                      ),
                    ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Profile header with image and username
                Padding(
                  padding: const EdgeInsets.only(top: 100),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Spacer(),
                      SizedBox(
                        child: Column(
                          children: [
                            // Profile image
                            Stack(
                              children: [
                                pfpProvider != null
                                    ? ClipOval(
                                        child: Material(
                                          color: Colors
                                              .transparent, // to show image background
                                          child: Ink.image(
                                            image: pfpProvider!,
                                            fit: BoxFit.cover,
                                            width: 120,
                                            height: 120,
                                            child: InkWell(
                                              onTap: _showTakePhotoDialog,
                                              splashColor: Colors.blue
                                                  .withValues(
                                                    alpha: 0.3,
                                                  ),
                                              borderRadius:
                                                  BorderRadius.circular(
                                                    60,
                                                  ),
                                            ),
                                          ),
                                        ),
                                      )
                                    : ClipOval(
                                        child: Material(
                                          color: Color.fromARGB(
                                            255,
                                            203,
                                            249,
                                            243,
                                          ), // cream yellow
                                          child: InkWell(
                                            onTap: _showTakePhotoDialog,
                                            splashColor: Colors.blue.withValues(
                                              alpha: 0.3,
                                            ),
                                            borderRadius: BorderRadius.circular(
                                              60,
                                            ),
                                            child: SizedBox(
                                              width: 120,
                                              height: 120,
                                            ),
                                          ),
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
                            Row(
                              children: [
                                Text(
                                  userEntity?.displayName ?? '',
                                  style: TextStyle(
                                    fontSize: 18.0,
                                    fontWeight: FontWeight.bold,
                                    fontFamily: 'OpenSans',
                                  ),
                                ),
                                if (userEntity?.source == Source.local) ...[
                                  SizedBox(
                                    width: 4,
                                  ),
                                  Icon(Icons.cloud_off_outlined),
                                ],
                              ],
                            ),
                          ],
                        ),
                      ),
                      Expanded(
                        child: Align(
                          alignment: Alignment.bottomRight,
                          child: IconButton(
                            onPressed: userEntity != null
                                ? () => _openEditProfileOverlay(userEntity)
                                : () {},
                            icon: Icon(Icons.edit),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    (userEntity?.headline ?? '').isEmpty
                        ? TextButton(
                            onPressed: () =>
                                _openEditProfileOverlay(userEntity!),
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
                      fallbackAction: () =>
                          _openEditProfileOverlay(userEntity!),
                    ),
                    ContactItem(
                      label: 'Mobile Number',
                      value: userEntity?.mobileNumber,
                      fallback: 'Add mobile number',
                      fallbackAction: () =>
                          _openEditProfileOverlay(userEntity!),
                    ),
                    ContactItem(
                      label: 'Major',
                      value: userEntity?.major,
                      fallback: 'Add major',
                      fallbackAction: () =>
                          _openEditProfileOverlay(userEntity!),
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
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: Column(
                        children: [
                          Stack(
                            children: [
                              AddElementWidget(
                                title: 'Add CV',
                                onTap: _pickAndUploadCVs,
                              ),
                              if (pendingCVUploads > 0)
                                Positioned(
                                  top: 8,
                                  right: 8,
                                  child: Container(
                                    padding: const EdgeInsets.all(6),
                                    decoration: const BoxDecoration(
                                      color: Colors.orange,
                                      shape: BoxShape.circle,
                                    ),
                                    child: Text(
                                      '$pendingCVUploads',
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 12,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                  ),
                                ),
                            ],
                          ),
                          const SizedBox(height: 8),
                          TextButton(
                            onPressed: _isOnline ? _viewUploadedCVs : null,
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Text(
                                  'View My CVs',
                                  style: TextStyle(
                                    color: _isOnline
                                        ? Colors.blue
                                        : Colors.grey,
                                  ),
                                ),
                                if (!_isOnline) ...[
                                  const SizedBox(width: 4),
                                  const Icon(
                                    Icons.cloud_off,
                                    size: 16,
                                    color: Colors.grey,
                                  ),
                                ],
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 16.0),
                    Expanded(
                      child: AddElementWidget(
                        title: 'Add Portfolio',
                        onTap: _openAddPortfolioOverlay,
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
        ],
      ),
    );
  }
}
