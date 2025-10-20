import 'package:talent_bridge_fl/cache/profile_cache.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

Future<({String name, String? photoUrl})> getProfileSnippetCacheFirst(
  String uid,
) async {
  final cache = ProfileCache.instance;
  final fresh = cache.getIfFresh(uid);
  if (fresh != null) return fresh;

  final user = await FirebaseService.instance.getUserById(uid);
  final name = user.displayName ?? 'User';
  final photo = user.photoUrl;
  cache.put(uid, name: name, photoUrl: photo);
  return (name: name, photoUrl: photo);
}
