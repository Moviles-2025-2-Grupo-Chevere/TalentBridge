import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; // input formatters
import 'package:firebase_auth/firebase_auth.dart'; // solo para capturar FirebaseAuthException (UX de errores)
import 'package:talent_bridge_fl/views/main-feed/main_feed.dart';
import 'package:talent_bridge_fl/views/signup/signup.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

// ---------- UI TOKENS ----------
const kBg = Color(0xFFFEF7E6); // cream background
const kAmber = Color(0xFFFFC107); // field border & button
const kBrandGreen = Color(0xFF568C73); // title
const kLinkGreen = Color(0xFF5E8F5A); // bottom link
const kShadowCol = Color(0x33000000); // 20% black for soft shadows
const kRadius = 22.0; // rounded corners

// ---------- GMAIL LIMITS ----------
const int kMaxGmailLocal = 30; // max chars before @gmail.com
const int kGmailSuffixLen = 10; // length of "@gmail.com"

// ---------- VALIDATORS ----------
String? _gmailOnly(String? v) {
  final value = (v ?? '').trim();
  if (value.isEmpty) return 'Enter your email';

  // Hard cap total length (local + "@gmail.com")
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

// ---------- DECORATION HELPERS ----------
InputDecoration _roundedInput({
  required String hint,
  IconData? icon,
  Widget? suffix,
}) {
  return InputDecoration(
    hintText: hint,
    prefixIcon: icon != null ? Icon(icon, color: kAmber) : null,
    suffixIcon: suffix,
    filled: true,
    fillColor: Colors.white,
    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kRadius),
      borderSide: const BorderSide(color: kAmber, width: 2),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kRadius),
      borderSide: const BorderSide(color: kAmber, width: 3),
    ),
    errorBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kRadius),
      borderSide: BorderSide(color: Colors.redAccent, width: 2),
    ),
    focusedErrorBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kRadius),
      borderSide: BorderSide(color: Colors.redAccent, width: 3),
    ),
  );
}

// Soft drop shadow wrapper
Widget _shadowWrap(Widget child) {
  return Container(
    decoration: BoxDecoration(
      borderRadius: BorderRadius.circular(kRadius),
      boxShadow: const [
        BoxShadow(
          color: kShadowCol,
          offset: Offset(0, 4),
          blurRadius: 8,
          spreadRadius: 0,
        ),
      ],
    ),
    child: child,
  );
}

// ---------- SCREEN ----------
class Login extends StatefulWidget {
  const Login({super.key});

  @override
  State<Login> createState() => _LoginState();
}

class _LoginState extends State<Login> {
  final _formKey = GlobalKey<FormState>();
  final _emailCtrl = TextEditingController();
  final _passCtrl = TextEditingController();

  final _isValid = ValueNotifier<bool>(false);
  final _obscure = ValueNotifier<bool>(true);

  // Façade
  final _fb = FirebaseService();

  @override
  void initState() {
    super.initState();
    _emailCtrl.addListener(_revalidate);
    _passCtrl.addListener(_revalidate);

    // (Opcional) registrar vista de pantalla para analytics
    // WidgetsBinding.instance.addPostFrameCallback((_) {
    //   _fb.logEvent('screen_view', {'screen': 'Login'});
    // });

    // Post-frame revalidation (covers autofill/paste)
    WidgetsBinding.instance.addPostFrameCallback((_) => _revalidate());
  }

  @override
  void dispose() {
    _emailCtrl.dispose();
    _passCtrl.dispose();
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
      await _fb.signIn(_emailCtrl.text.trim(), _passCtrl.text);

      if (!mounted) return;

      // Ir a MainFeed y limpiar el stack
      Navigator.of(context).pushAndRemoveUntil(
        MaterialPageRoute(builder: (_) => MainFeed()),
        (route) => false,
      );
    } on FirebaseAuthException catch (e) {
      // Conservamos tu UX de errores con códigos específicos
      final msg = switch (e.code) {
        'user-not-found' => 'No existe usuario con ese email.',
        'wrong-password' => 'Contraseña incorrecta.',
        'invalid-credential' => 'Credenciales inválidas.',
        'too-many-requests' => 'Demasiados intentos. Intenta más tarde.',
        _ => e.message ?? 'Error al iniciar sesión',
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
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // ---------- LOGO ----------
                    Center(
                      child: Image.asset(
                        'assets/images/talent_bridge_logo.png',
                        height: 240,
                      ),
                    ),

                    const SizedBox(height: 12),

                    // ---------- EMAIL ----------
                    Text('Email', style: labelStyle),
                    const SizedBox(height: 6),

                    _shadowWrap(
                      TextFormField(
                        controller: _emailCtrl,
                        keyboardType: TextInputType.emailAddress,
                        textInputAction: TextInputAction.next,
                        autofillHints: const [AutofillHints.email],
                        inputFormatters: [
                          LengthLimitingTextInputFormatter(
                            kMaxGmailLocal + kGmailSuffixLen,
                          ),
                          FilteringTextInputFormatter.deny(RegExp(r'\s')),
                        ],
                        decoration: _roundedInput(
                          hint: '',
                          icon: Icons.email_outlined,
                        ),
                        validator: _gmailOnly,
                        onChanged: (_) => _revalidate(),
                        onEditingComplete: _revalidate,
                      ),
                    ),

                    const SizedBox(height: 16),

                    // ---------- PASSWORD ----------
                    Text('Password', style: labelStyle),
                    const SizedBox(height: 6),

                    ValueListenableBuilder<bool>(
                      valueListenable: _obscure,
                      builder: (_, isObscure, __) => _shadowWrap(
                        TextFormField(
                          controller: _passCtrl,
                          obscureText: isObscure,
                          textInputAction: TextInputAction.done,
                          autofillHints: const [AutofillHints.password],
                          inputFormatters: [
                            LengthLimitingTextInputFormatter(64),
                          ],
                          decoration: _roundedInput(
                            hint: '',
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
                          onFieldSubmitted: (_) => _submit(),
                        ),
                      ),
                    ),

                    const SizedBox(height: 22),

                    // ---------- SUBMIT BUTTON ----------
                    ValueListenableBuilder<bool>(
                      valueListenable: _isValid,
                      builder: (_, ok, __) => Center(
                        child: SizedBox(
                          width: 180,
                          height: 48,
                          child: DecoratedBox(
                            decoration: const BoxDecoration(
                              boxShadow: [
                                BoxShadow(
                                  color: kShadowCol,
                                  offset: Offset(0, 6),
                                  blurRadius: 12,
                                ),
                              ],
                              borderRadius: BorderRadius.all(
                                Radius.circular(24),
                              ),
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
                              child: const Text('Sign in'),
                            ),
                          ),
                        ),
                      ),
                    ),

                    const SizedBox(height: 22),

                    // ---------- GMAIL ROUND BUTTON ----------
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

                    const SizedBox(height: 16),

                    // ---------- BOTTOM LINK ----------
                    Center(
                      child: TextButton(
                        onPressed: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(builder: (_) => const Signup()),
                          );
                        },
                        style: TextButton.styleFrom(
                          foregroundColor: kLinkGreen,
                          textStyle: const TextStyle(
                            decoration: TextDecoration.underline,
                          ),
                        ),
                        child: const Text(
                          "Don't have an account? Create account",
                        ),
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
