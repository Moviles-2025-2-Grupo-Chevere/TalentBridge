class _CacheItem {
  final String name;
  final String? photoUrl;
  final DateTime expiresAt;
  _CacheItem(this.name, this.photoUrl, this.expiresAt);

  bool get isFresh => DateTime.now().isBefore(expiresAt);
}

class ProfileCache {
  ProfileCache._();
  static final ProfileCache instance = ProfileCache._();

  final _map = <String, _CacheItem>{};
  Duration ttl = const Duration(minutes: 5);

  ({String name, String? photoUrl})? getIfFresh(String uid) {
    final it = _map[uid];
    if (it == null || !it.isFresh) return null;
    return (name: it.name, photoUrl: it.photoUrl);
  }

  ({String name, String? photoUrl})? getStale(String uid) {
    final it = _map[uid];
    if (it == null) return null;
    return (name: it.name, photoUrl: it.photoUrl);
  }

  void put(String uid, {required String name, String? photoUrl}) {
    _map[uid] = _CacheItem(name, photoUrl, DateTime.now().add(ttl));
  }

  void invalidate(String uid) => _map.remove(uid);
  void clear() => _map.clear();
}
