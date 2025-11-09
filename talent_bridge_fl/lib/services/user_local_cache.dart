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

  static Future<void> saveUser(UserEntity user) async {
    if (user.id == null || user.id!.isEmpty) {
      print('UserLocalCache: user.id vacío, no se guarda en cache.');
      return;
    }

    final prefs = await SharedPreferences.getInstance();
    final key = _keyForUid(user.id!);

    try {
      // 👇 Mapa "seguro": SOLO tipos simples (String, List<String>, etc.)
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
        // OJO: NO guardamos projects ni nada con Timestamp aquí
      };

      final jsonStr = jsonEncode(map);
      await prefs.setString(key, jsonStr);

      print('UserLocalCache: guardado en cache local uid=${user.id}');
    } catch (e) {
      print('UserLocalCache: error serializando user ${user.id}: $e');
    }
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
