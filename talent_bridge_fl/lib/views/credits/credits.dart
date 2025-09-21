import 'package:flutter/material.dart';

// ---- Tokens (same palette you’ve been using) ----
const kBg         = Color(0xFFFEF7E6); // cream
const kAmber      = Color(0xFFFFC107); // title accent
const kBrandGreen = Color(0xFF568C73); // brand tint / fallback
const kShadowCol  = Color(0x33000000); // soft shadow
const kPillRadius = 26.0;

// Optional: match the teal-ish Home button from your mock
const kHomeBtn = Color(0xFF2E6674);

class Credits extends StatefulWidget {
  const Credits({super.key});

  @override
  State<Credits> createState() => _CreditsState();
}

class _CreditsState extends State<Credits> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: kBg,
      body: SafeArea(
        child: Column(
          children: [
            // ---------- Header (cream bar with small logo) ----------
            Container(
              height: 64,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              decoration: const BoxDecoration(
                color: kBg,
                boxShadow: [
                  BoxShadow(
                    color: kShadowCol,
                    offset: Offset(0, 1),
                    blurRadius: 4,
                  ),
                ],
              ),
              child: Row(
                children: [
                  Image.asset(
                    'assets/images/talent_bridge_logo.png',
                    height: 100,
                    errorBuilder: (_, __, ___) =>
                        const Icon(Icons.account_balance, size: 28, color: kBrandGreen),
                  ),
                  const Spacer(),
                  // (You can add icons here later if needed)
                ],
              ),
            ),

            // ---------- Body ----------
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.fromLTRB(16, 20, 16, 24),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // ---- Hero: big logo (left) + "The Team" (right) ----
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        // Big logo
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
                        // "The Team" stacked text
                        Expanded(
                          child: Text(
                            'The\nTeam',
                            textAlign: TextAlign.left,
                            style: Theme.of(context).textTheme.headlineLarge?.copyWith(
                                  color: kAmber,
                                  height: 1.0,
                                  fontWeight: FontWeight.w700,
                                ),
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(height: 24),

                    // ---- Centered pill "Home" button with shadow ----
                    Center(
                      child: SizedBox(
                        width: 120,
                        height: 40,
                        child: DecoratedBox(
                          decoration: const BoxDecoration(
                            boxShadow: [
                              BoxShadow(
                                color: kShadowCol,
                                offset: Offset(0, 6),
                                blurRadius: 12,
                              ),
                            ],
                            borderRadius: BorderRadius.all(Radius.circular(22)),
                          ),
                          child: ElevatedButton(
                            onPressed: () {
                              Navigator.of(context).pop(); // back to previous (PrototypeMenu)
                            },
                            style: ElevatedButton.styleFrom(
                              backgroundColor: kHomeBtn,
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(22),
                              ),
                              elevation: 0,
                              textStyle: const TextStyle(fontWeight: FontWeight.w600),
                            ),
                            child: const Text('Home'),
                          ),
                        ),
                      ),
                    ),

                    // (Step 3 will add the alternating circular photos + names list)
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
