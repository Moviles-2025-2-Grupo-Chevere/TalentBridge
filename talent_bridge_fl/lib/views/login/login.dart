import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; 

String? _gmailOnly(String? v) {
  final value = (v ?? '').trim();
  if (value.isEmpty) return 'Insert your email';
  if (value.length > 254) return 'Maximum 254 characters';
  if (!value.toLowerCase().endsWith('@gmail.com')) {
    return 'must be a @gmail.com email';
  }
  final local = value.substring(0, value.length - '@gmail.com'.length);
  final localOk = RegExp(r"^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+$").hasMatch(local);
  if (!localOk || local.isEmpty) return 'Invalid email';
  return null;
}

String? _password(String? v, {int min = 8, int max = 64}) {
  final value = v ?? '';
  if (value.isEmpty) return 'Insert your password';
  if (value.length < min) return 'Minimum $min characters';
  if (value.length > max) return 'Maximum $max characters';
  return null;
}

class Login extends StatefulWidget {
  const Login({ super.key });

  @override
  State<Login> createState() => _LoginState();
}

class _LoginState extends State<Login> {

  // Form key so can call validate() on all fields at once
  final _formKey = GlobalKey<FormState>();
  final _emailCtrl = TextEditingController();
  final _passCtrl = TextEditingController();


   @override
  void dispose() {
    // Always disposing controllers to free resources
    _emailCtrl.dispose();
    _passCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context){
    return Scaffold(
      backgroundColor: const Color(0xFFFEF7E6), // bg
      body: SafeArea(
        // SafeArea keeps content away from notches/status bars
        child: Center(
          child: SingleChildScrollView(
            // Allows scrolling when the keyboard opens on small screens
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 16),
            child: Form(
              key: _formKey,
              // Show validation errors as the user interacts
              autovalidateMode: AutovalidateMode.onUserInteraction,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch, // full-width button
                children: [
                  

                  // ---------- EMAIL FIELD ----------
                  TextFormField(
                    controller: _emailCtrl,
                    keyboardType: TextInputType.emailAddress,
                    inputFormatters: [
                      LengthLimitingTextInputFormatter(254),         // max 254 chars
                      FilteringTextInputFormatter.deny(RegExp(r'\s')) // no spaces
                    ],
                    decoration: const InputDecoration(
                      hintText: 'your_email@gmail.com',
                      prefixIcon: Icon(Icons.email_outlined),
                      filled: true,
                      fillColor: Colors.white,
                    ),
                    validator: _gmailOnly, // uses the validator above
                  ),
                  const SizedBox(height: 12),

                  // ---------- PASSWORD FIELD ----------
                  TextFormField(
                    controller: _passCtrl,
                    obscureText: true, // hidden; we'll add the eye toggle in Step 4
                    inputFormatters: [LengthLimitingTextInputFormatter(64)], // max 64 chars
                    decoration: const InputDecoration(
                      hintText: '●●●●●●●●',
                      prefixIcon: Icon(Icons.lock_outline),
                      filled: true,
                      fillColor: Colors.white,
                    ),
                    validator: _password, // uses the validator above
                  ),
                  const SizedBox(height: 24),

                  // ---------- SUBMIT BUTTON ----------
                  // In this step the button is always enabled; this lets you see error messages.
                  ElevatedButton(
                    onPressed: () {
                      // Run all field validators
                      final ok = _formKey.currentState?.validate() ?? false;
                      if (ok) {
                        // "Happy path" proof with no backend yet: show a SnackBar
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('Valid Forms — Ready to authenticate')),
                        );
                      }
                    },
                    child: const Text('Log in'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}