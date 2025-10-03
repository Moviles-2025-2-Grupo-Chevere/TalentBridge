import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:talent_bridge_fl/components/add_element_widget.dart';
import 'package:talent_bridge_fl/components/yellow_text_box_widget.dart';
import 'package:talent_bridge_fl/components/circular_image_widget.dart';
import 'package:talent_bridge_fl/services/profile_storage.dart';

class MyProfile extends StatefulWidget {
  const MyProfile({Key? key}) : super(key: key);

  @override
  State<MyProfile> createState() => _MyProfileState();
}

class _MyProfileState extends State<MyProfile> {
  String? _profileImagePath;

  @override
  void initState() {
    super.initState();
    // Load saved profile image path
    _profileImagePath = ProfileStorage.getProfileImagePath();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      color: const Color.fromARGB(255, 255, 255, 255), // White background
      child: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Profile header with image and username
              Center(
                child: Column(
                  children: [
                    // Profile image
                    CircularImageWidget(
                      imageUrl:
                          _profileImagePath ?? 'assets/images/my_profile.jpg',
                      size: 120.0,
                      onTap: _showTakePhotoDialog,
                    ),
                    const SizedBox(height: 16.0),
                    // Username
                    const Text(
                      'Casey Neistat',
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
                'Contacto',
                style: TextStyle(
                  color: Color(0xFF3E6990),
                  fontSize: 18.0,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8.0),
              Center(
                child: Column(
                  children: [
                    _buildContactItem('Email:', 'usuario123@gmail.com'),
                    _buildContactItem('LinkedIn:', 'usuario123'),
                    _buildContactItem(
                      'Number:',
                      'Agregar número',
                      isLink: true,
                      linkColor: Colors.blue,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24.0),

              // Description section
              const Text(
                'Tu Descripción',
                style: TextStyle(
                  color: Color(0xFF3E6990),
                  fontSize: 18.0,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8.0),
              const Text(
                'Interesado en proyectos con paga con relación a la IA.',
                style: TextStyle(fontSize: 14.0),
              ),
              const SizedBox(height: 8.0),
              const Text(
                'Experiencia previa:',
                style: TextStyle(fontSize: 14.0),
              ),
              const Padding(
                padding: EdgeInsets.only(left: 16.0),
                child: Text('• Multiservi', style: TextStyle(fontSize: 14.0)),
              ),
              const SizedBox(height: 8.0),

              Center(
                child: Column(
                  children: [
                    _buildContactItem(
                      'Carrera:',
                      'Estudiante de Ing. Sistemas y Comp.',
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24.0),

              // Flags section (skill tags)
              const Text(
                'Mis Flags',
                style: TextStyle(
                  color: Color(0xFF3E6990),
                  fontSize: 18.0,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 12.0),
              Wrap(
                spacing: 8.0,
                runSpacing: 8.0,
                children: [
                  YellowTextBoxWidget(text: 'Diseño', onTap: () {}),
                  YellowTextBoxWidget(text: 'Dibujo', onTap: () {}),
                  YellowTextBoxWidget(text: '2 Horas', onTap: () {}),
                  YellowTextBoxWidget(text: 'Diseño', onTap: () {}),
                  YellowTextBoxWidget(text: 'Diseño', onTap: () {}),
                ],
              ),
              Center(
                child: TextButton(
                  onPressed: () {},
                  child: const Text(
                    'Agregar flag',
                    style: TextStyle(
                      color: Colors.blue,
                      decoration: TextDecoration.underline,
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
                  color: Color(0xFF3E6990),
                  fontSize: 18.0,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8.0),
              Center(
                child: Column(
                  children: [
                    Text(
                      'No tienes proyectos activos.',
                      style: TextStyle(
                        fontSize: 14.0,
                        color: Colors.grey[600],
                      ),
                    ),
                    const SizedBox(height: 16.0),
                    InkWell(
                      onTap: () {
                        // Add project action
                      },
                      child: Container(
                        width: 80.0,
                        height: 80.0,
                        decoration: BoxDecoration(
                          color: const Color.fromARGB(255, 185, 184, 184),
                          borderRadius: BorderRadius.circular(10.0),
                        ),
                        child: const Center(
                          child: Icon(
                            Icons.add,
                            color: Colors.grey,
                            size: 40.0,
                          ),
                        ),
                      ),
                    ),
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

  // Helper method to create contact information items
  Widget _buildContactItem(
    String label,
    String value, {
    bool isLink = false,
    Color? linkColor,
  }) {
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
            isLink
                ? InkWell(
                    onTap: () {
                      // Handle link tap
                    },
                    child: Text(
                      value,
                      style: TextStyle(
                        color: linkColor ?? Colors.blue,
                        decoration: TextDecoration.underline,
                        fontSize: 14.0,
                      ),
                    ),
                  )
                : Text(
                    value,
                    style: const TextStyle(fontSize: 14.0),
                  ),
          ],
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
