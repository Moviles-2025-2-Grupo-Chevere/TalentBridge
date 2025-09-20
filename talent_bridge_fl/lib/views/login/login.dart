import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; 

const kBg        = Color(0xFFFEF7E6); // cream background
const kAmber     = Color(0xFFFFC107); // field border & button
const kBrandGreen= Color(0xFF568C73); // title
const kLinkGreen = Color(0xFF5E8F5A); // bottom link
const kShadowCol = Color(0x33000000); // 20% black for soft shadows
const kRadius    = 22.0;              // rounded corners

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
      borderSide: BorderSide(color: Colors.red.shade400, width: 2),
    ),
    focusedErrorBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(kRadius),
      borderSide: BorderSide(color: Colors.red.shade400, width: 3),
    ),
  );
}

// Wraps a child with a soft drop shadow & rounded clip, to mimic your inputs’ shadow
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
  const Login({ super.key });

  @override
  State<Login> createState() => _LoginState();
}

class _LoginState extends State<Login> {
  final _formKey  = GlobalKey<FormState>();
  final _emailCtrl= TextEditingController();
  final _passCtrl = TextEditingController();

  final _isValid  = ValueNotifier<bool>(false);
  final _obscure  = ValueNotifier<bool>(true);

  @override
  void initState() {
    super.initState();
    _emailCtrl.addListener(_revalidate);
    _passCtrl.addListener(_revalidate);
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
        const SnackBar(content: Text('Formulario válido — listo para autenticar')),
      );
      // Next: plug real auth here.
    }
  }

  @override
  Widget build(BuildContext context){
    final labelStyle = Theme.of(context)
        .textTheme
        .bodyMedium
        ?.copyWith(color: kAmber);

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
                    Center(
                        child: Image.asset(
                          'assets/images/talent_bridge_logo.png',
                          height: 240,
                        ),
                      ),

                    

                    // ---------- EMAIL LABEL ----------
                    Text('Usuario', style: labelStyle),
                    const SizedBox(height: 6),

                    // ---------- EMAIL FIELD WITH SHADOW ----------
                    _shadowWrap(
                      TextFormField(
                        controller: _emailCtrl,
                        keyboardType: TextInputType.emailAddress,
                        textInputAction: TextInputAction.next,
                        autofillHints: const [AutofillHints.email],
                        inputFormatters: [
                          LengthLimitingTextInputFormatter(254),
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

                    // ---------- PASSWORD LABEL ----------
                    Text('Contraseña', style: labelStyle),
                    const SizedBox(height: 6),

                    // ---------- PASSWORD FIELD WITH EYE + SHADOW ----------
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
                              icon: Icon(isObscure ? Icons.visibility_off : Icons.visibility),
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

                    // ---------- SUBMIT BUTTON (AMBER, ROUNDED, SHADOW) ----------
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
                              borderRadius: BorderRadius.all(Radius.circular(24)),
                            ),
                            child: ElevatedButton(
                              onPressed: ok ? _submit : null,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: kAmber,
                                foregroundColor: Colors.white,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(24),
                                ),
                                elevation: 0, // we use custom shadow above
                                textStyle: const TextStyle(fontWeight: FontWeight.w600),
                              ),
                              child: const Text('Ingresar'),
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
                            const SnackBar(content: Text('Google Sign-In (pendiente)')),
                          );
                        },
                        child: Container(
                          width: 64,
                          height: 64,
                          decoration: BoxDecoration(
                            color: Colors.white,
                            shape: BoxShape.circle,
                            boxShadow: const [
                              BoxShadow(
                                color: kShadowCol,
                                offset: Offset(0, 6),
                                blurRadius: 12,
                              ),
                            ],
                          ),
                          alignment: Alignment.center,
                          // Replace with your Gmail asset if you have one:
                          // child: Image.asset('assets/icons/gmail.png', width: 32, height: 32),
                          child: const Icon(Icons.mail, size: 32, color: Colors.redAccent),
                        ),
                      ),
                    ),

                    const SizedBox(height: 16),

                    // ---------- BOTTOM LINK ----------
                    Center(
                      child: TextButton(
                        onPressed: () {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text('Navegar a Crear cuenta (pendiente)')),
                          );
                        },
                        style: TextButton.styleFrom(
                          foregroundColor: kLinkGreen,
                          textStyle: const TextStyle(decoration: TextDecoration.underline),
                        ),
                        child: const Text('¿No tienes una cuenta? Crear cuenta'),
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