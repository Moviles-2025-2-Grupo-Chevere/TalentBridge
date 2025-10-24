import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/views/home_view.dart';
import 'package:talent_bridge_fl/views/login/login.dart';

const kBg = Color(0xFFFEF7E6);

/// Uses a stream suscription to redirect users from the splash
/// screen to either home or the login
class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  late StreamSubscription<User?> _authState;

  @override
  void initState() {
    super.initState();
    _authState = FirebaseAuth.instance.authStateChanges().listen(
      (user) {
        if (user != null) {
          debugPrint('User data obtained');
          if (mounted) {
            Navigator.of(
              context,
            ).pushReplacement(MaterialPageRoute(builder: (_) => HomeView()));
          }
        } else {
          debugPrint('User data not found');
          if (mounted) {
            Navigator.of(
              context,
            ).pushReplacement(MaterialPageRoute(builder: (_) => Login()));
          }
        }
      },
    );
  }

  @override
  void dispose() {
    _authState.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: kBg,
      body: Center(
        child: Image.asset(
          'assets/images/MainAppIcon.png',
          height: 250,
          fit: BoxFit.contain,
        ),
      ),
    );
  }
}
