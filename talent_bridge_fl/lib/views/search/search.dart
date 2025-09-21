import 'package:flutter/material.dart';

// ---- Tokens (local to Search for now) ----
const kBg         = Color(0xFFFEF7E6); // cream
const kBrandGreen = Color(0xFF568C73); // (logo tint / optional)
const kShadowCol  = Color(0x33000000); // soft shadow

class Search extends StatefulWidget {
  const Search({super.key});

  @override
  State<Search> createState() => _SearchState();
}

class _SearchState extends State<Search> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: kBg,
      body: SafeArea(
        child: Column(
          children: [
            // ---------- Header (logo left, icons right) ----------
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
                  // App logo
                  Image.asset(
                    'assets/images/talent_bridge_logo.png',
                    height: 28,
                    errorBuilder: (_, __, ___) => const Icon(
                      Icons.account_balance,
                      size: 28,
                      color: kBrandGreen,
                    ),
                  ),
                  const Spacer(),

                  // Profile icon
                  IconButton(
                    icon: const Icon(Icons.account_circle_outlined),
                    onPressed: () {
                      // TODO: navigate to profile
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Profile (pending)')),
                      );
                    },
                  ),

                  // Menu icon
                  IconButton(
                    icon: const Icon(Icons.menu),
                    onPressed: () {
                      // TODO: open drawer/menu
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Menu (pending)')),
                      );
                    },
                  ),
                ],
              ),
            ),

            // ---------- Body placeholder (we'll add search UI in Step 3) ----------
            const Expanded(
              child: Center(
                child: Text(
                  'Search view — header ready',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
