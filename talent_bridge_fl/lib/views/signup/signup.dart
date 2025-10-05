import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:firebase_auth/firebase_auth.dart'; // para mapear errores conocidos
import 'package:talent_bridge_fl/views/login/login.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

// ---------- COLOR & SHAPE TOKENS ----------
const kBg = Color(0xFFFEF7E6); // cream background
const kAmber = Color(0xFFFFC107); // borders & CTA
const kBrandGreen = Color(0xFF568C73); // titles
const kLinkGreen = Color(0xFF5E8F5A); // links / helper text
const kShadowCol = Color(0x33000000); // 20% black shadow
const kPillRadius = 26.0; // pill look (≈ height/2)

// ---------- GMAIL LIMITS ----------
const int kMaxGmailLocal = 30; // max chars before @gmail.com
const int kGmailSuffixLen = 10; // length of "@gmail.com"

// ---------- VALIDATORS ----------
String? _username(String? v, {int min = 3, int max = 30}) {
  final value = (v ?? '').trim();
  if (value.isEmpty) return 'Enter your username';
  if (value.length < min) return 'Minimum $min characters';
  if (value.length > max) return 'Maximum $max characters';
  final ok = RegExp(r'^[A-Za-z0-9._-]+$').hasMatch(value);
  if (!ok) return 'Only letters, numbers, ".", "_" or "-"';
  return null;
}

String? _gmailOnly(String? v) {
  final value = (v ?? '').trim();
  if (value.isEmpty) return 'Enter your email';

  // TOTAL cap = local part + "@gmail.com"
  if (value.length > kMaxGmailLocal + kGmailSuffixLen) {
    return 'Max $kMaxGmailLocal characters before @gmail.com';
  }
  if (!value.toLowerCase().endsWith('@gmail.com')) {
    return 'Must be a @gmail.com email';
  }

  final local = value.substring(0, value.length - '@gmail.com'.length);
  if (local.isEmpty) return 'Invalid email';
  final localOk = RegExp(r"^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+$").hasMatch(local);
  if (!localOk) return 'Invalid email';
  if (local.length > kMaxGmailLocal) {
    return 'Max $kMaxGmailLocal characters before @gmail.com';
  }
  return null;
}

String? _password(String? v, {int min = 8, int max = 64}) {
  final value = v ?? '';
  if (value.isEmpty) return 'Enter your password';
  if (value.length < min) return 'Minimum $min characters';
  if (value.length > max) return 'Maximum $max characters';
  return null;
}

// ---------- UI HELPERS ----------
InputDecoration _pillInput({
  IconData? icon,
  Widget? suffix,
}) {
  return InputDecoration(
    prefixIcon: icon != null ? Icon(icon, color: kAmber) : null,
    suffixIcon: suffix,
    filled: true,
    fillColor: Colors.white,
    isDense: true,
    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kPillRadius),
      borderSide: const BorderSide(color: kAmber, width: 2),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kPillRadius),
      borderSide: const BorderSide(color: kAmber, width: 3),
    ),
    errorBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kPillRadius),
      borderSide: const BorderSide(color: Colors.redAccent, width: 2),
    ),
    focusedErrorBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kPillRadius),
      borderSide: const BorderSide(color: Colors.redAccent, width: 3),
    ),
  );
}

Widget _shadowWrap(Widget child) {
  return Container(
    decoration: BoxDecoration(
      borderRadius: BorderRadius.circular(kPillRadius),
      boxShadow: const [
        BoxShadow(
          color: kShadowCol,
          offset: Offset(0, 6),
          blurRadius: 12,
        ),
      ],
    ),
    child: child,
  );
}

// ---------- SCREEN ----------
class Signup extends StatefulWidget {
  const Signup({super.key});
  @override
  State<Signup> createState() => _SignupState();
}

class _SignupState extends State<Signup> {
  final _formKey = GlobalKey<FormState>();

  final _userCtrl = TextEditingController();
  final _passCtrl = TextEditingController();
  final _emailCtrl = TextEditingController();

  final _isValid = ValueNotifier<bool>(false);
  final _obscure = ValueNotifier<bool>(true);

  // Façade
  final _fb = FirebaseService();

  @override
  void initState() {
    super.initState();
    _userCtrl.addListener(_revalidate);
    _passCtrl.addListener(_revalidate);
    _emailCtrl.addListener(_revalidate);

    // (Opcional) registrar vista de pantalla
    // WidgetsBinding.instance.addPostFrameCallback((_) {
    //   _fb.logEvent('screen_view', {'screen': 'Signup'});
    // });
  }

  @override
  void dispose() {
    _userCtrl.dispose();
    _passCtrl.dispose();
    _emailCtrl.dispose();
    _isValid.dispose();
    _obscure.dispose();
    super.dispose();
  }

  void _revalidate() {
    final ok = _formKey.currentState?.validate() ?? false;
    if (ok != _isValid.value) _isValid.value = ok;
  }

  Future<void> _submit() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;

    final m = ScaffoldMessenger.of(context);
    m.hideCurrentSnackBar();

    try {
      await _fb.signUp(
        email: _emailCtrl.text.trim(),
        password: _passCtrl.text,
        displayName: _userCtrl.text.trim(),
      );

      if (!mounted) return;

      m.showSnackBar(const SnackBar(content: Text('Account created!')));
      // Si prefieres volver al Login automáticamente, descomenta:
      // Navigator.of(context).push(MaterialPageRoute(builder: (_) => const Login()));
    } on FirebaseAuthException catch (e) {
      final msg = switch (e.code) {
        'email-already-in-use' => 'Ese email ya está registrado.',
        'invalid-email' => 'Email inválido.',
        'weak-password' => 'La contraseña es muy débil.',
        _ => e.message ?? 'Error al registrarse',
      };
      m.showSnackBar(SnackBar(content: Text(msg)));
    } catch (e) {
      m.showSnackBar(SnackBar(content: Text('Error inesperado: $e')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final labelStyle = Theme.of(
      context,
    ).textTheme.bodyMedium?.copyWith(color: kAmber);

    return Scaffold(
      appBar: AppBar(backgroundColor: kBg,),
      backgroundColor: kBg,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 16),
            child: Form(
              key: _formKey,
              autovalidateMode: AutovalidateMode.onUserInteraction,
              child: AutofillGroup(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Title
                    Text(
                      'Sign Up',
                      textAlign: TextAlign.center,
                      style: Theme.of(context).textTheme.headlineLarge
                          ?.copyWith(
                            color: kAmber,
                            fontWeight: FontWeight.w700,
                          ),
                    ),
                    const SizedBox(height: 36),

                    // ----- USER -----
                    Text('User', style: labelStyle),
                    const SizedBox(height: 6),
                    _shadowWrap(
                      TextFormField(
                        controller: _userCtrl,
                        textInputAction: TextInputAction.next,
                        inputFormatters: [
                          LengthLimitingTextInputFormatter(30),
                          FilteringTextInputFormatter.deny(RegExp(r'\s')),
                        ],
                        decoration: _pillInput(icon: Icons.person_outline),
                        validator: _username,
                        onChanged: (_) => _revalidate(),
                        onEditingComplete: _revalidate,
                      ),
                    ),

                    const SizedBox(height: 16),

                    // ----- PASSWORD -----
                    Text('Password', style: labelStyle),
                    const SizedBox(height: 6),
                    ValueListenableBuilder<bool>(
                      valueListenable: _obscure,
                      builder: (_, isObscure, __) => _shadowWrap(
                        TextFormField(
                          controller: _passCtrl,
                          obscureText: isObscure,
                          textInputAction: TextInputAction.next,
                          inputFormatters: [
                            LengthLimitingTextInputFormatter(64),
                          ],
                          decoration: _pillInput(
                            icon: Icons.lock_outline,
                            suffix: IconButton(
                              onPressed: () => _obscure.value = !isObscure,
                              icon: Icon(
                                isObscure
                                    ? Icons.visibility_off
                                    : Icons.visibility,
                              ),
                              color: kAmber,
                            ),
                          ),
                          validator: _password,
                          onChanged: (_) => _revalidate(),
                          onEditingComplete: _revalidate,
                        ),
                      ),
                    ),

                    const SizedBox(height: 16),

                    // ----- EMAIL -----
                    Text('Email', style: labelStyle),
                    const SizedBox(height: 6),
                    _shadowWrap(
                      TextFormField(
                        controller: _emailCtrl,
                        textInputAction: TextInputAction.done,
                        keyboardType: TextInputType.emailAddress,
                        autofillHints: const [AutofillHints.email],
                        inputFormatters: [
                          LengthLimitingTextInputFormatter(
                            kMaxGmailLocal + kGmailSuffixLen,
                          ),
                          FilteringTextInputFormatter.deny(RegExp(r'\s')),
                        ],
                        decoration: _pillInput(icon: Icons.email_outlined),
                        validator: _gmailOnly,
                        onChanged: (_) => _revalidate(),
                        onEditingComplete: _revalidate,
                        onFieldSubmitted: (_) => _submit(),
                      ),
                    ),

                    const SizedBox(height: 26),

                    // ----- CTA "Next" -----
                    ValueListenableBuilder<bool>(
                      valueListenable: _isValid,
                      builder: (_, ok, __) => Center(
                        child: SizedBox(
                          width: 180,
                          height: 48,
                          child: DecoratedBox(
                            decoration: const BoxDecoration(
                              borderRadius: BorderRadius.all(
                                Radius.circular(24),
                              ),
                              boxShadow: [
                                BoxShadow(
                                  color: kShadowCol,
                                  offset: Offset(0, 6),
                                  blurRadius: 12,
                                ),
                              ],
                            ),
                            child: ElevatedButton(
                              onPressed: ok ? _submit : null,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: kAmber,
                                foregroundColor: Colors.white,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(24),
                                ),
                                elevation: 0,
                                textStyle: const TextStyle(
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              child: const Text('Next'),
                            ),
                          ),
                        ),
                      ),
                    ),

                    const SizedBox(height: 20),

                    // ----- "Other Sign In Options" -----
                    Center(
                      child: Text(
                        'Other Sign In Options',
                        style: Theme.of(
                          context,
                        ).textTheme.bodyMedium?.copyWith(color: kLinkGreen),
                      ),
                    ),
                    const SizedBox(height: 10),

                    // ----- Round Gmail button -----
                    Center(
                      child: InkWell(
                        borderRadius: BorderRadius.circular(32),
                        onTap: () {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Google Sign-In (pending)'),
                            ),
                          );
                        },
                        child: Container(
                          width: 64,
                          height: 64,
                          decoration: const BoxDecoration(
                            color: Colors.white,
                            shape: BoxShape.circle,
                            boxShadow: [
                              BoxShadow(
                                color: kShadowCol,
                                offset: Offset(0, 6),
                                blurRadius: 12,
                              ),
                            ],
                          ),
                          alignment: Alignment.center,
                          child: const Icon(
                            Icons.mail,
                            size: 32,
                            color: Colors.redAccent,
                          ),
                        ),
                      ),
                    ),

                    const SizedBox(height: 22),

                    // ----- Bottom link -----
                    Center(
                      child: TextButton(
                        onPressed: () {
                          Navigator.of(context).pop();
                        },
                        style: TextButton.styleFrom(
                          foregroundColor: kLinkGreen,
                          textStyle: const TextStyle(
                            decoration: TextDecoration.underline,
                          ),
                        ),
                        child: const Text('Already have an account? Log In'),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
