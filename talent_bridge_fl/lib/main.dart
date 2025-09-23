import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/views/prototype_menu.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Color.fromARGB(255, 97, 32, 37),
          surface: Color(0xFFFDFAE5),
        ),
        fontFamily: 'OpenSans',
      ),
      home: const PrototypeMenu(),
    );
  }
}
