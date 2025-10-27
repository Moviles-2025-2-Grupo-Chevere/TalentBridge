import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/text_box_widget.dart';
import 'package:talent_bridge_fl/analytics/analytics_timer.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';

class UserProfile extends StatefulWidget {
  const UserProfile({super.key, this.uid});
  final String? uid; // si no lo pasas, usa el current user

  @override
  State<UserProfile> createState() => _UserProfileState();
}

class _UserProfileState extends State<UserProfile> {
  late final ScreenTimer _tProfile;
  bool _sent = false;
  final _fb = FirebaseService();

  Future<UserEntity> _loadUser() async {
    final uid = widget.uid ?? _fb.currentUid();
    if (uid == null) throw Exception('No user id');
    return _fb.getUserById(uid);
  }

  @override
  void initState() {
    super.initState();
    _tProfile = ScreenTimer(
      'first_content_profile',
      baseParams: {'screen': 'Profile'},
    );
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      color: const Color.fromARGB(255, 255, 255, 255),
      child: FutureBuilder<UserEntity>(
        future: _loadUser(),
        builder: (_, snap) {
          if (snap.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snap.hasError) {
            return const Center(child: Text('Error loading profile'));
          }
          if (!snap.hasData) {
            return const Center(child: Text('Profile not found'));
          }

          // Primer contenido real -> cierra BQ una sola vez
          if (!_sent) {
            _sent = true;
            _tProfile.endOnce(
              source: 'unknown', // cambia a 'cache' si usas helper cache-first
              itemCount: 1,
            );
          }

          final u = snap.data!;
          return SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 20.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Center(
                  child: Column(
                    children: [
                      CircleAvatar(
                        radius: 40,
                        backgroundImage:
                            (u.photoUrl != null && u.photoUrl!.isNotEmpty)
                            ? NetworkImage(u.photoUrl!)
                            : null,
                        child: (u.photoUrl == null || u.photoUrl!.isEmpty)
                            ? const Icon(Icons.person, size: 40)
                            : null,
                      ),
                      const SizedBox(height: 16.0),
                      Text(
                        u.displayName.isNotEmpty ? u.displayName : 'Usuario',
                        style: const TextStyle(
                          fontSize: 18.0,
                          fontWeight: FontWeight.bold,
                          fontFamily: 'OpenSans',
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 24.0),
                Center(child: _buildContactItem('Carrera:', u.major ?? '—')),
                const SizedBox(height: 24.0),

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
                Text(
                  u.description?.isNotEmpty == true
                      ? u.description!
                      : 'Sin descripción',
                ),
                const SizedBox(height: 16.0),

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
                  children: (u.skillsOrTopics ?? [])
                      .take(8)
                      .map((s) => TextBoxWidget(text: s, onTap: () {}))
                      .toList(),
                ),
                const SizedBox(height: 24.0),

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
                      _buildContactItem('Email:', u.email, isLink: false),
                      if ((u.linkedin ?? '').isNotEmpty)
                        _buildContactItem(
                          'LinkedIn:',
                          u.linkedin!,
                          isLink: true,
                          linkColor: Colors.blue,
                        ),
                      if ((u.mobileNumber ?? '').isNotEmpty)
                        _buildContactItem(
                          'Number:',
                          u.mobileNumber!,
                          isLink: false,
                        ),
                    ],
                  ),
                ),
                const SizedBox(height: 24.0),
              ],
            ),
          );
        },
      ),
    );
  }

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
                      /* TODO: abrir link */
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
                : Text(value, style: const TextStyle(fontSize: 14.0)),
          ],
        ),
      ),
    );
  }
}
