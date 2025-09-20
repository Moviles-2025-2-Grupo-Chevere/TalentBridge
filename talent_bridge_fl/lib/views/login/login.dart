import 'package:flutter/material.dart';

String? _gmailOnly(String? v) {
  final value = (v ?? '').trim();
  if (value.isEmpty) return 'Ingresa tu correo';
  if (value.length > 254) return 'Máximo 254 caracteres';
  if (!value.toLowerCase().endsWith('@gmail.com')) {
    return 'Debe ser un correo @gmail.com';
  }
  final local = value.substring(0, value.length - '@gmail.com'.length);
  final localOk = RegExp(r"^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+$").hasMatch(local);
  if (!localOk || local.isEmpty) return 'Correo inválido';
  return null;
}

String? _password(String? v, {int min = 8, int max = 64}) {
  final value = v ?? '';
  if (value.isEmpty) return 'Ingresa tu contraseña';
  if (value.length < min) return 'Mínimo $min caracteres';
  if (value.length > max) return 'Máximo $max caracteres';
  return null;
}

class Login extends StatefulWidget {
  const Login({ super.key });

  @override
  State<Login> createState() => _LoginState();
}

class _LoginState extends State<Login> {
  @override
  Widget build(BuildContext context){
    return const Scaffold(
      backgroundColor: Color(0xFFFEF7E6),
      body: SafeArea(
        child: Center(
          child: Text('Login view (scaffolded)'),
        ),
      ),
    );
  }
}
