import 'package:flutter/material.dart';

// ---------- VALIDATORS (top-level; no UI yet) ----------

// Username: 3–30 chars, letters/numbers/._- allowed
String? _username(String? v, {int min = 3, int max = 30}) {
  final value = (v ?? '').trim();
  if (value.isEmpty) return 'Ingresa tu usuario';
  if (value.length < min) return 'Mínimo $min caracteres';
  if (value.length > max) return 'Máximo $max caracteres';
  final ok = RegExp(r'^[A-Za-z0-9._-]+$').hasMatch(value);
  if (!ok) return 'Solo letras, números, ".", "_" o "-"';
  return null; // valid
}

// Gmail-only email: <=254 chars and valid local part
String? _gmailOnly(String? v) {
  final value = (v ?? '').trim();
  if (value.isEmpty) return 'Ingresa tu correo';
  if (value.length > 254) return 'Máximo 254 caracteres';
  if (!value.toLowerCase().endsWith('@gmail.com')) {
    return 'Debe ser un correo @gmail.com';
  }
  final local = value.substring(0, value.length - '@gmail.com'.length);
  final localOk =
      RegExp(r"^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+$").hasMatch(local);
  if (!localOk || local.isEmpty) return 'Correo inválido';
  return null; // valid
}

// Password: 8–64 chars
String? _password(String? v, {int min = 8, int max = 64}) {
  final value = v ?? '';
  if (value.isEmpty) return 'Ingresa tu contraseña';
  if (value.length < min) return 'Mínimo $min caracteres';
  if (value.length > max) return 'Máximo $max caracteres';
  return null; // valid
}

class Signup extends StatefulWidget {
  const Signup({super.key});

  @override
  State<Signup> createState() => _SignupState();
}

class _SignupState extends State<Signup> {
  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: Color(0xFFFEF7E6), 
      body: SafeArea(
        child: Center(
          child: Text(
            'Create your account',
            style: TextStyle(
              fontSize: 28,
              fontWeight: FontWeight.w700,
              color: Color(0xFFFFC107),
            ),
          ),
        ),
      ),
    );
  }
}
