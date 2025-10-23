import 'package:flutter/material.dart';

// ---- Tokens ----
const kBg = Color(0xFFFEF7E6); // cream
const kAmber = Color(0xFFFFC107); // title & names
const kBrandGreen = Color(0xFF568C73); // brand tint / fallback
const kShadowCol = Color(0x33000000); // soft shadow
const kPillRadius = 26.0;
const kHomeBtn = Color(0xFF2E6674);

// Simple data holder for each teammate
class _Member {
  final String name;
  final String? asset; // e.g., 'assets/images/daniel.png'; null => placeholder
  const _Member(this.name, this.asset);
}

class Credits extends StatefulWidget {
  const Credits({super.key});

  @override
  State<Credits> createState() => _CreditsState();
}

class _CreditsState extends State<Credits> {
  // Edit these to point to your real asset files (leave null for placeholder)
  final List<_Member> _members = const [
    _Member('Daniel', 'assets/images/DANIEL.JPG'),
    _Member('David', 'assets/images/DAVID.JPG'),
    _Member('Mariana', 'assets/images/MARIANA.JPG'),
    _Member('Manuela', 'assets/images/MANUELA.JPG'),
    _Member('Juan Diego', 'assets/images/MP.JPG'), // map as you need
    _Member('María Pau', 'assets/images/MP.JPG'),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Credits'),
      ),
      backgroundColor: kBg,
      body: SafeArea(
        child: Column(
          children: [
            // ---------- Body ----------
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.fromLTRB(16, 20, 16, 24),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        Expanded(
                          child: Center(
                            child: Image.asset(
                              'assets/images/talent_bridge_logo.png',
                              height: 120,
                              errorBuilder: (_, __, ___) => const Icon(
                                Icons.account_balance,
                                size: 120,
                                color: kBrandGreen,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Text(
                            'The\nTeam',
                            style: Theme.of(context).textTheme.headlineLarge
                                ?.copyWith(
                                  color: kAmber,
                                  height: 1.0,
                                  fontWeight: FontWeight.w700,
                                ),
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(height: 28),

                    // ---- Team members (alternating left/right) ----
                    ...List.generate(_members.length, (i) {
                      final m = _members[i];
                      final even = i.isEven;

                      // avatar widget with placeholder fallback
                      Widget avatar;
                      if (m.asset == null) {
                        avatar = const CircleAvatar(
                          radius: 34,
                          backgroundColor: Color(0xFFE0E0E0),
                          child: Icon(
                            Icons.person,
                            color: Colors.white,
                            size: 28,
                          ),
                        );
                      } else {
                        avatar = CircleAvatar(
                          radius: 34,
                          backgroundColor: Colors.grey.shade200,
                          backgroundImage: AssetImage(m.asset!),
                          onBackgroundImageError: (_, __) {},
                        );
                      }

                      final nameText = Text(
                        m.name,
                        style: const TextStyle(
                          color: kAmber,
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                        ),
                      );

                      return Padding(
                        padding: const EdgeInsets.symmetric(vertical: 14),
                        child: Row(
                          mainAxisAlignment: even
                              ? MainAxisAlignment.start
                              : MainAxisAlignment.end,
                          children: [
                            if (even) ...[
                              avatar,
                              const SizedBox(width: 16),
                              nameText,
                            ] else ...[
                              nameText,
                              const SizedBox(width: 16),
                              avatar,
                            ],
                          ],
                        ),
                      );
                    }),

                    // (Optional) bottom decorative logo like your mock:
                    const SizedBox(height: 32),
                    const Center(
                      child: Icon(
                        Icons.account_balance,
                        color: kBrandGreen,
                        size: 28,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
