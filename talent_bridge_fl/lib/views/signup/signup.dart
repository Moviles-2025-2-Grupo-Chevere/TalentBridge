import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

// ---------- GMAIL LIMITS ----------
const int kMaxGmailLocal = 30;   // max characters BEFORE @gmail.com
const int kGmailSuffixLen = 10;  // "@gmail.com".length

// ---------- VALIDATORS ----------

// Username: 3–30 chars, letters/numbers/._- allowed
String? _username(String? v, {int min = 3, int max = 30}) {
  final value = (v ?? '').trim();
  if (value.isEmpty) return 'Enter your username';
  if (value.length < min) return 'Minimum $min characters';
  if (value.length > max) return 'Maximum $max characters';
  final ok = RegExp(r'^[A-Za-z0-9._-]+$').hasMatch(value);
  if (!ok) return 'Only letters, numbers, ".", "_" or "-"';
  return null;
}

// Gmail-only email with strict length caps
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

  final localOk =
      RegExp(r"^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+$").hasMatch(local);
  if (!localOk) return 'Invalid email';

  if (local.length > kMaxGmailLocal) {
    return 'Max $kMaxGmailLocal characters before @gmail.com';
  }
  return null;
}

// Password: 8–64
String? _password(String? v, {int min = 8, int max = 64}) {
  final value = v ?? '';
  if (value.isEmpty) return 'Enter your password';
  if (value.length < min) return 'Minimum $min characters';
  if (value.length > max) return 'Maximum $max characters';
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

  final _userCtrl  = TextEditingController();
  final _passCtrl  = TextEditingController();
  final _emailCtrl = TextEditingController();

  // Enable/disable button + password visibility
  final _isValid = ValueNotifier<bool>(false);
  final _obscure = ValueNotifier<bool>(true);

  @override
  void initState() {
    super.initState();
    _userCtrl.addListener(_revalidate);
    _passCtrl.addListener(_revalidate);
    _emailCtrl.addListener(_revalidate);
    // Revalidate after first frame (covers autofill/paste)
    WidgetsBinding.instance.addPostFrameCallback((_) => _revalidate());
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

  void _submit() {
    if (_formKey.currentState?.validate() ?? false) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Valid form — ready to create account')),
      );
      // Next: call your real signup API here.
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
              child: AutofillGroup(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Title
                    Text(
                      'Create Your Account',
                      textAlign: TextAlign.center,
                      style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                            color: const Color(0xFFFFC107),
                            fontWeight: FontWeight.w700,
                          ),
                    ),
                    const SizedBox(height: 24),

                    // Username
                    TextFormField(
                      controller: _userCtrl,
                      textInputAction: TextInputAction.next,
                      inputFormatters: [
                        LengthLimitingTextInputFormatter(30),
                        FilteringTextInputFormatter.deny(RegExp(r'\s')),
                      ],
                      decoration: const InputDecoration(
                        hintText: 'Username',
                        prefixIcon: Icon(Icons.person_outline),
                        filled: true,
                        fillColor: Colors.white,
                      ),
                      validator: _username,
                      onChanged: (_) => _revalidate(),
                      onEditingComplete: _revalidate,
                    ),
                    const SizedBox(height: 12),

                    // Password (with eye toggle)
                    ValueListenableBuilder<bool>(
                      valueListenable: _obscure,
                      builder: (_, isObscure, __) => TextFormField(
                        controller: _passCtrl,
                        obscureText: isObscure,
                        textInputAction: TextInputAction.next,
                        inputFormatters: [LengthLimitingTextInputFormatter(64)],
                        decoration: InputDecoration(
                          hintText: 'Password',
                          prefixIcon: const Icon(Icons.lock_outline),
                          filled: true,
                          fillColor: Colors.white,
                          suffixIcon: IconButton(
                            onPressed: () => _obscure.value = !isObscure,
                            icon: Icon(isObscure ? Icons.visibility_off : Icons.visibility),
                          ),
                        ),
                        validator: _password,
                        onChanged: (_) => _revalidate(),
                        onEditingComplete: _revalidate,
                      ),
                    ),
                    const SizedBox(height: 12),

                    // Email (gmail only, hard length cap)
                    TextFormField(
                      controller: _emailCtrl,
                      textInputAction: TextInputAction.done,
                      keyboardType: TextInputType.emailAddress,
                      autofillHints: const [AutofillHints.email],
                      inputFormatters: [
                        LengthLimitingTextInputFormatter(kMaxGmailLocal + kGmailSuffixLen),
                        FilteringTextInputFormatter.deny(RegExp(r'\s')),
                      ],
                      decoration: const InputDecoration(
                        hintText: 'Email (gmail only)',
                        prefixIcon: Icon(Icons.email_outlined),
                        filled: true,
                        fillColor: Colors.white,
                      ),
                      validator: _gmailOnly,
                      onChanged: (_) => _revalidate(),
                      onEditingComplete: _revalidate,
                      onFieldSubmitted: (_) => _submit(),
                    ),
                    const SizedBox(height: 24),

                    // Create (disabled until valid)
                    ValueListenableBuilder<bool>(
                      valueListenable: _isValid,
                      builder: (_, ok, __) => ElevatedButton(
                        onPressed: ok ? _submit : null,
                        child: const Text('Create'),
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
