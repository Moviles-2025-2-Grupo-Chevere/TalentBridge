import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:talent_bridge_fl/domain/member_entity.dart';

class MemberCacheService {
  static const String _cacheKey = 'members_cache';
  static const String _timestampKey = 'members_cache_timestamp';
  static const int _maxCacheAge =
      7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds

  /// Save members to cache
  Future<void> cacheMembers(List<MemberEntity> members) async {
    try {
      final prefs = await SharedPreferences.getInstance();

      // Convert members list to JSON
      final membersJson = members.map((m) => m.toMap()).toList();
      final jsonString = jsonEncode(membersJson);

      // Save to SharedPreferences
      await prefs.setString(_cacheKey, jsonString);
      await prefs.setInt(_timestampKey, DateTime.now().millisecondsSinceEpoch);

      print('Cached ${members.length} members');
    } catch (e) {
      print('Error caching members: $e');
    }
  }

  /// Get cached members
  Future<List<MemberEntity>?> getCachedMembers() async {
    try {
      final prefs = await SharedPreferences.getInstance();

      // Check if cache exists
      final jsonString = prefs.getString(_cacheKey);
      if (jsonString == null) {
        print('No cached members found');
        return null;
      }

      // Check cache age
      final timestamp = prefs.getInt(_timestampKey) ?? 0;
      final age = DateTime.now().millisecondsSinceEpoch - timestamp;

      if (age > _maxCacheAge) {
        print('Cache expired');
        await clearCache();
        return null;
      }

      // Parse cached data
      final List<dynamic> membersJson = jsonDecode(jsonString);
      final members = membersJson
          .map((json) => MemberEntity.fromMap(json as Map<String, dynamic>))
          .toList();

      print('Retrieved ${members.length} cached members');
      return members;
    } catch (e) {
      print('Error getting cached members: $e');
      return null;
    }
  }

  /// Check if cache is valid
  Future<bool> isCacheValid() async {
    try {
      final prefs = await SharedPreferences.getInstance();

      if (!prefs.containsKey(_cacheKey)) {
        return false;
      }

      final timestamp = prefs.getInt(_timestampKey) ?? 0;
      final age = DateTime.now().millisecondsSinceEpoch - timestamp;

      return age <= _maxCacheAge;
    } catch (e) {
      return false;
    }
  }

  /// Clear cache
  Future<void> clearCache() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove(_cacheKey);
      await prefs.remove(_timestampKey);
      print('Cache cleared');
    } catch (e) {
      print('Error clearing cache: $e');
    }
  }
}
