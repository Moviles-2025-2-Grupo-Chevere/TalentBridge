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

// ---------- LOGIN SCREEN ----------
class Login extends StatefulWidget {
  const Login({ super.key });

  @override
  State<Login> createState() => _LoginState();
}

class _LoginState extends State<Login> {
  final _formKey = GlobalKey<FormState>();
  final _emailCtrl = TextEditingController();
  final _passCtrl = TextEditingController();

  // Button enable + password visibility
  final _isValid = ValueNotifier<bool>(false);
  final _obscure = ValueNotifier<bool>(true);

  @override
  void initState() {
    super.initState();

    // Revalidate whenever text changes (covers typing AND most pastes)
    _emailCtrl.addListener(_revalidate);
    _passCtrl.addListener(_revalidate);

    // Also revalidate right after the first frame.
    // This catches cases where autofill sets values without firing listeners immediately.
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

  void _submit() {
    if (_formKey.currentState?.validate() ?? false) {
      final m = ScaffoldMessenger.of(context);
      m.hideCurrentSnackBar();
      m.showSnackBar(
        const SnackBar(content: Text('Valid Forms Ready to Authenticate')),
      );
      // Next: plug real auth here.
    }
  }

  @override
  Widget build(BuildContext context){
    return Scaffold(
      backgroundColor: const Color(0xFFFEF7E6),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 16),
            child: Form(
              key: _formKey,
              autovalidateMode: AutovalidateMode.onUserInteraction,
              // Group fields so platform autofill behaves correctly
              child: AutofillGroup(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                   

                    // ---------- EMAIL ----------
                    TextFormField(
                      controller: _emailCtrl,
                      keyboardType: TextInputType.emailAddress,
                      textInputAction: TextInputAction.next,
                      autofillHints: const [AutofillHints.email],
                      inputFormatters: [
                        LengthLimitingTextInputFormatter(254),
                        FilteringTextInputFormatter.deny(RegExp(r'\s')),
                      ],
                      decoration: const InputDecoration(
                        hintText: 'your_email@gmail.com',
                        prefixIcon: Icon(Icons.email_outlined),
                        filled: true,
                        fillColor: Colors.white,
                      ),
                      validator: _gmailOnly,

                      // NEW: make sure we revalidate on any edit/IME event
                      onChanged: (_) => _revalidate(),
                      onEditingComplete: _revalidate,
                    ),
                    const SizedBox(height: 12),

                    // ---------- PASSWORD (with eye toggle) ----------
                    ValueListenableBuilder<bool>(
                      valueListenable: _obscure,
                      builder: (_, isObscure, __) => TextFormField(
                        controller: _passCtrl,
                        obscureText: isObscure,
                        textInputAction: TextInputAction.done,
                        autofillHints: const [AutofillHints.password],
                        inputFormatters: [LengthLimitingTextInputFormatter(64)],
                        decoration: InputDecoration(
                          hintText: '●●●●●●●●',
                          prefixIcon: const Icon(Icons.lock_outline),
                          filled: true,
                          fillColor: Colors.white,
                          suffixIcon: IconButton(
                            onPressed: () => _obscure.value = !isObscure,
                            icon: Icon(isObscure ? Icons.visibility_off : Icons.visibility),
                          ),
                        ),
                        validator: _password,

                        // NEW: also revalidate on any edit/IME event
                        onChanged: (_) => _revalidate(),
                        onEditingComplete: _revalidate,

                        // Pressing "done" triggers submit
                        onFieldSubmitted: (_) => _submit(),
                      ),
                    ),
                    const SizedBox(height: 24),

                    // ---------- SUBMIT (enabled only if form is valid) ----------
                    ValueListenableBuilder<bool>(
                      valueListenable: _isValid,
                      builder: (_, ok, __) => ElevatedButton(
                        onPressed: ok ? _submit : null,
                        child: const Text('Log in'),
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