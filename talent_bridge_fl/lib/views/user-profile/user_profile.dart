import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/text_box_widget.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/providers/profile_provider.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/analytics/analytics_timer.dart';

class UserProfile extends ConsumerWidget {
  const UserProfile({super.key, this.userId});
  final String? userId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final asyncUser = userId != null
        ? ref.watch(userProfileStreamProvider(userId!))
        : ref.watch(remoteProfileProvider).whenData((doc) {
            final map = doc.data() ?? <String, dynamic>{};
            return UserEntity.fromMap({...map, 'id': doc.id});
          });

    return asyncUser.when(
      loading: () => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      error: (e, _) => Scaffold(
        appBar: AppBar(),
        body: Center(child: Text('Error: $e')),
      ),
      data: (user) {
        final displayName = user.displayName.isNotEmpty
            ? user.displayName
            : 'Usuario';
        final headline = (user.headline ?? '').isNotEmpty
            ? user.headline!
            : '-';
        final carrera = (user.major ?? '').isNotEmpty ? user.major! : '-';
        final email = user.email.isNotEmpty ? user.email : '-';
        final linkedin = (user.linkedin ?? '').isNotEmpty
            ? user.linkedin!
            : '-';
        final number = (user.mobileNumber ?? '').isNotEmpty
            ? user.mobileNumber!
            : '-';
        final desc = (user.description ?? '').isNotEmpty
            ? user.description!
            : '-';
        final photoUrl = (user.photoUrl ?? '').isNotEmpty
            ? user.photoUrl!
            : null;
        final skills = user.skillsOrTopics ?? const <String>[];

        return _BQFirstFrameProbe(
          eventName: 'first_content_profile',
          baseParams: const {'screen': 'Profile'},
          source: userId != null ? 'user_by_id' : 'current_user',
          child: Container(
            color: const Color.fromARGB(255, 255, 255, 255),
            child: SingleChildScrollView(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Header
                    const SizedBox(height: 16),
                    Center(
                      child: Column(
                        children: [
                          CircleAvatar(
                            radius: 40,
                            backgroundColor: const Color(0xFFEFEFEF),
                            backgroundImage: photoUrl != null
                                ? NetworkImage(photoUrl)
                                : const AssetImage('assets/images/pfp.png')
                                      as ImageProvider,
                          ),
                          const SizedBox(height: 16.0),
                          Text(
                            displayName,
                            style: const TextStyle(
                              fontSize: 18.0,
                              fontWeight: FontWeight.bold,
                              fontFamily: 'OpenSans',
                            ),
                          ),
                          if (headline != '-')
                            Padding(
                              padding: const EdgeInsets.only(top: 4.0),
                              child: Text(
                                headline,
                                style: const TextStyle(fontSize: 14.0),
                              ),
                            ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 24.0),

                    Center(child: _buildContactItem('Carrera:', carrera)),
                    const SizedBox(height: 24.0),

                    // Descripción
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
                    Text(desc, style: const TextStyle(fontSize: 14.0)),
                    const SizedBox(height: 8.0),

                    // Flags
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
                      children: skills.isEmpty
                          ? [const Text('—')]
                          : skills
                                .map(
                                  (s) => TextBoxWidget(text: s, onTap: () {}),
                                )
                                .toList(),
                    ),

                    // Contacto
                    const SizedBox(height: 16.0),
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
                          _buildContactItem('Email:', email, isLink: false),
                          _buildContactItem(
                            'LinkedIn:',
                            linkedin,
                            isLink: true,
                            linkColor: Colors.blue,
                          ),
                          _buildContactItem(
                            'Number:',
                            number,
                            isLink: true,
                            linkColor: Colors.blue,
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 24.0),
                  ],
                ),
              ),
            ),
          ),
        );
      },
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

/// ---- Mini wrapper stateful SOLO para la BQ ----
/// Arranca el cronómetro y lo cierra en el primer frame visible.
/// No toca tu lógica ni tu UI.
class _BQFirstFrameProbe extends StatefulWidget {
  const _BQFirstFrameProbe({
    required this.child,
    required this.eventName,
    this.baseParams = const {},
    this.source = 'unknown',
  });

  final Widget child;
  final String eventName;
  final Map<String, Object?> baseParams;
  final String source;

  @override
  State<_BQFirstFrameProbe> createState() => _BQFirstFrameProbeState();
}

class _BQFirstFrameProbeState extends State<_BQFirstFrameProbe> {
  late final ScreenTimer _t;
  bool _sent = false;

  @override
  void initState() {
    super.initState();
    _t = ScreenTimer(widget.eventName, baseParams: widget.baseParams);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_sent) {
        _sent = true;
        _t.endOnce(source: widget.source, itemCount: 1);
      }
    });
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
