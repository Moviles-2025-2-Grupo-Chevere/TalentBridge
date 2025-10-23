import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/add_element_widget.dart';
import 'package:talent_bridge_fl/components/text_box_widget.dart';

class OrgProfile extends StatelessWidget {
  const OrgProfile({Key? key}) : super(key: key);

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
                    const CircleAvatar(),
                    const SizedBox(height: 16.0),
                    // Username
                    const Text(
                      'Organizacion123',
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
                    _buildContactItem('Email:', 'organizacion123@gmail.com'),
                    _buildContactItem('LinkedIn:', 'organizacion123'),
                    _buildContactItem(
                      'Instagram:',
                      'Agregar Instagram',
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
                'Iniciativa de IA.',
                style: TextStyle(fontSize: 14.0),
              ),
              const SizedBox(height: 8.0),
              const Text(
                'Aceptamos estudiantes de todas las carreras y semestres!',
                style: TextStyle(fontSize: 14.0),
              ),
              const SizedBox(height: 8.0),

              Center(
                child: Column(
                  children: [
                    _buildContactItem(
                      'Carrera:',
                      'Multidisciplinaria',
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
                  TextBoxWidget(text: 'Diseño', onTap: () {}),
                  TextBoxWidget(text: 'Dibujo', onTap: () {}),
                  TextBoxWidget(text: 'Robótica', onTap: () {}),
                  TextBoxWidget(text: 'ROS', onTap: () {}),
                  TextBoxWidget(text: 'MatLab', onTap: () {}),
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
                      title: 'Agregar Oferta',
                      onTap: () {
                        // Add CV action
                      },
                    ),
                  ),
                  const SizedBox(width: 16.0),
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
}
