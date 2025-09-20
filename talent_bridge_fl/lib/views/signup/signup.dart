import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

// ---------- LIMITS (tweak as needed) ----------
const int kMaxGmailLocal = 30;                 // max chars BEFORE @gmail.com
const int kGmailSuffixLen = 10;                // "@gmail.com".length

// ---------- VALIDATORS (top-level) ----------

// Username: 3–30 chars, letters/numbers/._- allowed
String? _username(String? v, {int min = 3, int max = 30}) {
  final value = (v ?? '').trim();
  if (value.isEmpty) return 'Ingresa tu usuario';
  if (value.length < min) return 'Mínimo $min caracteres';
  if (value.length > max) return 'Máximo $max caracteres';
  final ok = RegExp(r'^[A-Za-z0-9._-]+$').hasMatch(value);
  if (!ok) return 'Solo letras, números, ".", "_" o "-"';
  return null;
}

// Gmail-only email with strict length: total ≤ local+suffix, local ≤ kMaxGmailLocal
String? _gmailOnly(String? v) {
  final value = (v ?? '').trim();
  if (value.isEmpty) return 'Ingresa tu correo';

  // Hard cap for TOTAL length: local + "@gmail.com"
  if (value.length > kMaxGmailLocal + kGmailSuffixLen) {
    return 'Máximo $kMaxGmailLocal caracteres antes de @gmail.com';
  }

  if (!value.toLowerCase().endsWith('@gmail.com')) {
    return 'Debe ser un correo @gmail.com';
  }

  final local = value.substring(0, value.length - '@gmail.com'.length);
  if (local.isEmpty) return 'Correo inválido';

  // Allowed chars for local part
  final localOk =
      RegExp(r"^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+$").hasMatch(local);
  if (!localOk) return 'Correo inválido';

  // Local part length check
  if (local.length > kMaxGmailLocal) {
    return 'Máximo $kMaxGmailLocal caracteres antes de @gmail.com';
  }

  return null;
}

// Password: 8–64 chars
String? _password(String? v, {int min = 8, int max = 64}) {
  final value = v ?? '';
  if (value.isEmpty) return 'Ingresa tu contraseña';
  if (value.length < min) return 'Mínimo $min caracteres';
  if (value.length > max) return 'Máximo $max caracteres';
  return null;
}

// ---------- SIGNUP SCREEN ----------
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

  @override
  void dispose() {
    _userCtrl.dispose();
    _passCtrl.dispose();
    _emailCtrl.dispose();
    super.dispose();
  }

  void _submit() {
    final ok = _formKey.currentState?.validate() ?? false;
    if (ok) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Formulario válido — listo para crear cuenta')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFEF7E6), // same cream bg as Login
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 16),
            child: Form(
              key: _formKey,
              autovalidateMode: AutovalidateMode.onUserInteraction,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Title
                  Text(
                    'Crea Tu Cuenta',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                          color: const Color(0xFFFFC107), // amber
                          fontWeight: FontWeight.w700,
                        ),
                  ),
                  const SizedBox(height: 24),

                  // Usuario
                  TextFormField(
                    controller: _userCtrl,
                    textInputAction: TextInputAction.next,
                    inputFormatters: [
                      LengthLimitingTextInputFormatter(30),
                      FilteringTextInputFormatter.deny(RegExp(r'\s')), // no spaces
                    ],
                    decoration: const InputDecoration(
                      hintText: 'Usuario',
                      prefixIcon: Icon(Icons.person_outline),
                      filled: true,
                      fillColor: Colors.white,
                    ),
                    validator: _username,
                  ),
                  const SizedBox(height: 12),

                  // Contraseña
                  TextFormField(
                    controller: _passCtrl,
                    textInputAction: TextInputAction.next,
                    obscureText: true,
                    inputFormatters: [LengthLimitingTextInputFormatter(64)],
                    decoration: const InputDecoration(
                      hintText: 'Contraseña',
                      prefixIcon: Icon(Icons.lock_outline),
                      filled: true,
                      fillColor: Colors.white,
                    ),
                    validator: _password,
                  ),
                  const SizedBox(height: 12),

                  // Email (gmail only, hard length cap)
                  TextFormField(
                    controller: _emailCtrl,
                    textInputAction: TextInputAction.done,
                    keyboardType: TextInputType.emailAddress,
                    autofillHints: const [AutofillHints.email],
                    inputFormatters: [
                      // TOTAL length cap = local + "@gmail.com"
                      LengthLimitingTextInputFormatter(kMaxGmailLocal + kGmailSuffixLen),
                      FilteringTextInputFormatter.deny(RegExp(r'\s')),
                    ],
                    decoration: const InputDecoration(
                      hintText: 'Email (solo @gmail.com)',
                      prefixIcon: Icon(Icons.email_outlined),
                      filled: true,
                      fillColor: Colors.white,
                    ),
                    validator: _gmailOnly,
                    onFieldSubmitted: (_) => _submit(),
                  ),
                  const SizedBox(height: 24),

                  // Crear (enabled in this step)
                  ElevatedButton(
                    onPressed: _submit,
                    child: const Text('Crear'),
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
