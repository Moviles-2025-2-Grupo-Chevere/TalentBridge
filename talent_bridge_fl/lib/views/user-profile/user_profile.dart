import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/text_box_widget.dart';

class UserProfile extends StatelessWidget {
  const UserProfile({super.key});

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
                      'UsuarioXYZ',
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
              Center(
                child: Column(
                  children: [
                    _buildContactItem(
                      'Carrera:',
                      'Diseño',
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24.0),

              // Description section
              const Text(
                'Descripción',
                style: TextStyle(
                  color: Color(0xFF3E6990),
                  fontSize: 18.0,
                  fontWeight: FontWeight.bold,
                  fontFamily: 'OpenSans',
                  height: 1.5,
                ),
              ),
              const SizedBox(height: 8.0),
              const Text(
                'Interesado en proyectos no pagos (por experiencia) de diseño gráfico, edición de video y desarrollo web. Busco oportunidades para aprender y crecer en estas áreas.',
                style: TextStyle(fontSize: 14.0),
              ),
              const SizedBox(height: 8.0),
              const Text(
                'Experiencia previa:',
                style: TextStyle(fontSize: 14.0),
              ),
              const Padding(
                padding: EdgeInsets.only(left: 16.0),
                child: Text(
                  '• Monitor de investigación de la facultad de Arquitectura y Diseño',
                  style: TextStyle(fontSize: 14.0),
                ),
              ),
              const SizedBox(height: 8.0),

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
                  TextBoxWidget(text: 'AutoCad', onTap: () {}),
                  TextBoxWidget(text: 'Planos', onTap: () {}),
                  TextBoxWidget(text: 'Cerámica', onTap: () {}),
                ],
              ),

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
                    _buildContactItem(
                      'Email:',
                      'usuario123@gmail.com',
                      isLink: true,
                      linkColor: Colors.blue,
                    ),
                    _buildContactItem(
                      'LinkedIn:',
                      'usuario123',
                      isLink: true,
                      linkColor: Colors.blue,
                    ),
                    _buildContactItem(
                      'Number:',
                      '+39 1234 567890',
                      isLink: true,
                      linkColor: Colors.blue,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24.0),

              // Link sections for CV and Portfolio

              // Projects section
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
