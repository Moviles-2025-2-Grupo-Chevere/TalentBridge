import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';

class CommentsLocalHelper {
  static const _keyPrefix = 'comments_cache_';

  static Future<void> saveProjectComments(
    String projectId,
    List<Map<String, dynamic>> comments,
  ) async {
    final prefs = await SharedPreferences.getInstance();
    final key = '$_keyPrefix$projectId';
    final jsonStr = jsonEncode(comments);
    await prefs.setString(key, jsonStr);
  }

  static Future<List<Map<String, dynamic>>> getProjectComments(
    String projectId,
  ) async {
    final prefs = await SharedPreferences.getInstance();
    final key = '$_keyPrefix$projectId';
    final jsonStr = prefs.getString(key);
    if (jsonStr == null) return [];
    try {
      final List decoded = jsonDecode(jsonStr);
      return decoded.map((e) => Map<String, dynamic>.from(e)).toList();
    } catch (_) {
      return [];
    }
  }
}
