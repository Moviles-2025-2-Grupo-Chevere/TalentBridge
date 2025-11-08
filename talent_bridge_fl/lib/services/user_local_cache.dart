// Servicio de cache local para datos de usuario (NO foto de perfil).
// En esta fase SOLO guardamos y leemos UserEntity desde SharedPreferences.
// Aún no está conectado a ninguna pantalla.

import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';

class UserLocalCache {
  // Prefijo para evitar chocar con otras keys en SharedPreferences
  static const _prefix = 'user_cache_';

  /// Construye la clave interna a partir del uid
  static String _keyForUid(String uid) => '$_prefix$uid';

  /// Guarda un usuario en cache local.
  ///
  /// Se guarda como JSON (String) en SharedPreferences con clave = 'user_cache_<uid>'.
  static Future<void> saveUser(UserEntity user) async {
    if (user.id == null || user.id!.isEmpty) {
      // Si por alguna razón no hay id, no guardamos nada
      print('UserLocalCache: user.id vacío, no se guarda en cache.');
      return;
    }

    final prefs = await SharedPreferences.getInstance();
    final key = _keyForUid(user.id!);

    // Mapa "ligero" para guardar en cache.
    // Usamos los campos que ya vimos que existen en UserEntity / Firestore.
    final Map<String, dynamic> map = {
      'id': user.id,
      'displayName': user.displayName,
      'email': user.email,
      'headline': user.headline,
      'major': user.major,
      'linkedin': user.linkedin,
      'mobileNumber': user.mobileNumber,
      'description': user.description,
      'skillsOrTopics': user.skillsOrTopics,
      'projects': user.projects?.map((p) => p.toMap()).toList(),
      // Puedes agregar más campos si los necesitas luego.
    };

    final jsonStr = jsonEncode(map);
    await prefs.setString(key, jsonStr);
    print('UserLocalCache: guardado en cache local uid=${user.id}');
  }

  /// Lee un usuario del cache local por uid.
  ///
  /// Devuelve null si no hay nada guardado para ese uid o si falla el parseo.
  static Future<UserEntity?> getUser(String uid) async {
    if (uid.isEmpty) return null;

    final prefs = await SharedPreferences.getInstance();
    final key = _keyForUid(uid);

    final jsonStr = prefs.getString(key);
    if (jsonStr == null) {
      print('UserLocalCache: no hay cache para uid=$uid');
      return null;
    }

    try {
      final map = jsonDecode(jsonStr) as Map<String, dynamic>;
      // Reconstruimos el UserEntity usando el mismo fromMap que usas con Firestore
      final user = UserEntity.fromMap(map);
      print('UserLocalCache: cache encontrado para uid=$uid');
      return user;
    } catch (e) {
      print('UserLocalCache: error parseando cache de uid=$uid: $e');
      return null;
    }
  }

  /// Borra el cache local de un usuario específico (por uid).
  static Future<void> clearUser(String uid) async {
    if (uid.isEmpty) return;
    final prefs = await SharedPreferences.getInstance();
    final key = _keyForUid(uid);
    await prefs.remove(key);
    print('UserLocalCache: cache borrado para uid=$uid');
  }
}
