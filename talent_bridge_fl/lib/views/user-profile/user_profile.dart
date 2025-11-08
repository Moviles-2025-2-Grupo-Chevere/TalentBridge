import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:cached_network_image/cached_network_image.dart';

import 'package:talent_bridge_fl/providers/profile_provider.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/components/text_box_widget.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:talent_bridge_fl/analytics/analytics_timer.dart';

/// ----- Brand palette -----
class TBColors {
  static const cream = Color(0xFFFFF7E6); // background
  static const ink = Color(0xFF222222); // main text
  static const mute = Color(0xFF6B7280); // secondary text
  static const gold = Color(0xFFFFC300); // section titles
  static const blue = Color(0xFF3E6990); // accents/borders
  static const link = Color(0xFF0A66C2); // link color
}

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
        appBar: AppBar(
          backgroundColor: TBColors.cream,
          foregroundColor: TBColors.ink,
        ),
        body: Center(child: Text('Error: $e')),
      ),
      data: (user) => _BQFirstFrameProbe(
        eventName: 'first_content_profile',
        baseParams: const {'screen': 'Profile'},
        source: userId != null ? 'user_by_id' : 'current_user',
        child: Scaffold(
          appBar: AppBar(
            title: const Text('Profile'),
            backgroundColor: TBColors.cream,
            foregroundColor: TBColors.ink,
            elevation: 0,
          ),
          body: Container(
            color: TBColors.cream,
            child: SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
              child: _ProfileBody(user: user),
            ),
          ),
        ),
      ),
    );
  }
}

class _ProfileBody extends StatelessWidget {
  const _ProfileBody({required this.user});
  final UserEntity user;

  @override
  Widget build(BuildContext context) {
    final displayName = user.displayName.isNotEmpty ? user.displayName : 'User';
    final headline = (user.headline ?? '').trim();
    final carrera = (user.major ?? '').trim();
    final email = user.email.trim();
    final linkedin = (user.linkedin ?? '').trim();
    final number = (user.mobileNumber ?? '').trim();
    final desc = (user.description ?? '').trim();

    final photoUrl = (user.photoUrl ?? '').trim().isEmpty
        ? null
        : user.photoUrl;

    final skills = user.skillsOrTopics ?? const <String>[];
    final projects = user.projects ?? const [];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // ===== Header =====
        Center(
          child: Column(
            children: [
              _ProfileAvatar(
                uid: user.id ?? '',
                photoUrl: photoUrl,
              ),
              const SizedBox(height: 12),
              Text(
                displayName,
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  fontFamily: 'OpenSans',
                  color: TBColors.ink,
                ),
                textAlign: TextAlign.center,
              ),
              if (headline.isNotEmpty) const SizedBox(height: 4),
              if (headline.isNotEmpty)
                Text(
                  headline,
                  style: const TextStyle(fontSize: 14, color: TBColors.mute),
                  textAlign: TextAlign.center,
                ),
            ],
          ),
        ),

        const SizedBox(height: 20),

        // ===== Major =====
        if (carrera.isNotEmpty) ...[
          const _SectionTitle('Major'),
          _TagPill(carrera),
          const SizedBox(height: 20),
        ],

        // ===== Description =====
        const _SectionTitle('Description'),
        Text(
          desc.isNotEmpty ? desc : '—',
          style: const TextStyle(
            fontSize: 14,
            height: 1.4,
            color: TBColors.ink,
          ),
        ),
        const SizedBox(height: 20),

        // ===== Skills / Flags =====
        const _SectionTitle('My Flags'),
        const SizedBox(height: 8),
        skills.isEmpty
            ? const Text(
                '—',
                style: TextStyle(fontSize: 14, color: TBColors.ink),
              )
            : Wrap(
                spacing: 8,
                runSpacing: 8,
                children: skills
                    .map((s) => TextBoxWidget(text: s, onTap: () {}))
                    .toList(),
              ),
        const SizedBox(height: 20),

        // ===== Contact =====
        const _SectionTitle('Contact'),
        const SizedBox(height: 8),
        _ContactItem(
          label: 'Email',
          value: email.isNotEmpty ? email : '—',
          onTap: email.isNotEmpty
              ? () => _launchUri(Uri(scheme: 'mailto', path: email))
              : null,
        ),
        _ContactItem(
          label: 'LinkedIn',
          value: linkedin.isNotEmpty ? linkedin : '—',
          onTap: linkedin.isNotEmpty
              ? () {
                  final uri = linkedin.startsWith('http')
                      ? Uri.parse(linkedin)
                      : Uri.parse('https://www.linkedin.com/in/$linkedin');
                  _launchUri(uri);
                }
              : null,
        ),
        _ContactItem(
          label: 'Number',
          value: number.isNotEmpty ? number : '—',
          onTap: number.isNotEmpty
              ? () => _launchUri(Uri(scheme: 'tel', path: number))
              : null,
        ),
        const SizedBox(height: 24),

        // ===== Projects =====
        const _SectionTitle('Projects'),
        const SizedBox(height: 8),
        if (projects.isEmpty)
          const Text(
            'This user doesn\'t have published projects',
            style: TextStyle(color: TBColors.mute),
          )
        else
          Column(
            children: projects.map((p) {
              final created = p.createdAt;
              final subtitle = created != null ? 'Created: $created' : null;
              return Card(
                margin: const EdgeInsets.symmetric(vertical: 6),
                child: ListTile(
                  title: Text(
                    p.title,
                    style: const TextStyle(
                      fontWeight: FontWeight.w600,
                      color: TBColors.ink,
                    ),
                  ),
                  subtitle: subtitle != null
                      ? Text(
                          subtitle,
                          style: const TextStyle(color: TBColors.mute),
                        )
                      : null,
                  dense: true,
                ),
              );
            }).toList(),
          ),

        const SizedBox(height: 40),
      ],
    );
  }
}

/// Avatar with 3-level fallback: http photoUrl → Storage/profile_pictures/<uid> → asset
class _ProfileAvatar extends StatelessWidget {
  const _ProfileAvatar({
    super.key,
    required this.uid,
    required this.photoUrl,
  });

  final String uid;
  final String? photoUrl;

  @override
  Widget build(BuildContext context) {
    if (photoUrl != null && photoUrl!.startsWith('http')) {
      return _circle(CachedNetworkImageProvider(photoUrl!));
    }

    return FutureBuilder<String?>(
      future: FirebaseService().getPfpUrlByUid(uid),
      builder: (context, snap) {
        if (snap.connectionState == ConnectionState.waiting) {
          return const CircleAvatar(
            radius: 48,
            backgroundColor: TBColors.cream,
          );
        }
        if (snap.hasData && snap.data != null) {
          return _circle(CachedNetworkImageProvider(snap.data!));
        }
        return const CircleAvatar(
          radius: 48,
          backgroundImage: AssetImage('assets/images/pfp.png'),
          backgroundColor: TBColors.cream,
        );
      },
    );
  }

  Widget _circle(ImageProvider provider) => const CircleAvatar(
    radius: 48,
    backgroundColor: TBColors.cream,
    foregroundImage: null, // keep backgroundImage for compatibility
  ).copyWith(backgroundImage: provider);
}

extension on CircleAvatar {
  CircleAvatar copyWith({ImageProvider? backgroundImage}) => CircleAvatar(
    radius: radius,
    backgroundColor: backgroundColor,
    backgroundImage: backgroundImage ?? this.backgroundImage,
  );
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: const TextStyle(
        color: TBColors.gold,
        fontSize: 18,
        fontWeight: FontWeight.bold,
        fontFamily: 'OpenSans',
        height: 1.5,
      ),
    );
  }
}

class _ContactItem extends StatelessWidget {
  const _ContactItem({
    required this.label,
    required this.value,
    this.onTap,
  });

  final String label;
  final String value;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final clickable = onTap != null && value != '—';
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        children: [
          SizedBox(
            width: 86,
            child: Text(
              '$label:',
              style: const TextStyle(
                color: TBColors.gold,
                fontSize: 14,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: clickable
                ? InkWell(
                    onTap: onTap,
                    child: Text(
                      value,
                      style: const TextStyle(
                        decoration: TextDecoration.underline,
                        color: TBColors.link,
                        fontSize: 14,
                      ),
                    ),
                  )
                : Text(
                    value,
                    style: const TextStyle(fontSize: 14, color: TBColors.ink),
                  ),
          ),
        ],
      ),
    );
  }
}

class _TagPill extends StatelessWidget {
  const _TagPill(this.text);
  final String text;
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        border: Border.all(color: TBColors.blue),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        text,
        style: const TextStyle(fontSize: 12, color: TBColors.ink),
      ),
    );
  }
}

/// ---- Mini wrapper stateful ONLY for analytics timing ----
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

Future<void> _launchUri(Uri uri) async {
  if (await canLaunchUrl(uri)) {
    await launchUrl(uri, mode: LaunchMode.externalApplication);
  }
}
