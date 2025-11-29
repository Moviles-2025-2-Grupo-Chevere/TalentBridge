import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';

/// Entrada simple para guardar resultados de búsqueda:
/// solo necesitamos uid + displayName para reconstruir las tarjetas.
class SearchUserSummary {
  final String uid;
  final String displayName;

  SearchUserSummary({
    required this.uid,
    required this.displayName,
  });

  Map<String, dynamic> toMap() {
    return {
      'uid': uid,
      'displayName': displayName,
    };
  }

  factory SearchUserSummary.fromMap(Map<String, dynamic> map) {
    return SearchUserSummary(
      uid: map['uid'] as String? ?? '',
      displayName: map['displayName'] as String? ?? '',
    );
  }
}

/// Servicio para cachear la ÚLTIMA búsqueda de usuarios.
class SearchLocalCache {
  static const _lastSearchKey = 'search_last_results';

  /// Guarda una lista de usuarios como "resumen" (uid + nombre).
  static Future<void> saveLastUserResults(List<UserEntity> users) async {
    final prefs = await SharedPreferences.getInstance();

    final summaries = users
        .where((u) => (u.id ?? '').isNotEmpty)
        .map(
          (u) => SearchUserSummary(
            uid: u.id,
            displayName: u.displayName,
          ).toMap(),
        )
        .toList();

    final jsonStr = jsonEncode(summaries);
    await prefs.setString(_lastSearchKey, jsonStr);

    print('SearchLocalCache: guardados ${summaries.length} resultados.');
  }

  /// Lee la última lista de resultados cacheados.
  static Future<List<SearchUserSummary>> getLastUserResults() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_lastSearchKey);
    if (raw == null || raw.isEmpty) {
      print('SearchLocalCache: no hay resultados cacheados.');
      return [];
    }

    try {
      final List<dynamic> decoded = jsonDecode(raw) as List<dynamic>;
      final list = decoded
          .whereType<Map<String, dynamic>>()
          .map((m) => SearchUserSummary.fromMap(m))
          .toList();

      print('SearchLocalCache: leídos ${list.length} resultados cacheados.');
      return list;
    } catch (e) {
      print('SearchLocalCache: error al decodificar cache: $e');
      return [];
    }
  }

  /// (Opcional) limpiar el cache si alguna vez lo necesitas
  static Future<void> clearLastUserResults() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_lastSearchKey);
  }
}
