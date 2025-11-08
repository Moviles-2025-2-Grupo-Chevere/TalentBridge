import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

/// Avatar circular con cache:
/// 1) Muestra URL guardada en SharedPreferences (clave = uid)
/// 2) Refresca desde Firebase Storage y actualiza el cache (si hay red)
/// 3) Fallback a asset si nunca hubo red
class UserPfpCached extends StatefulWidget {
  const UserPfpCached({
    super.key,
    required this.uid,
    this.radius = 48,
    this.assetFallback = const AssetImage('assets/images/pfp.png'),
  });

  final String uid;
  final double radius;
  final ImageProvider assetFallback;

  @override
  State<UserPfpCached> createState() => _UserPfpCachedState();
}

class _UserPfpCachedState extends State<UserPfpCached> {
  String? _url; // url cacheada o remota
  late String _cacheKey; // usamos el uid como cacheKey estable

  @override
  void initState() {
    super.initState();
    _cacheKey = widget.uid;
    _load();
  }

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();

    // 1) Primero, si hay URL local, muéstrala
    final local = prefs.getString(_cacheKey);
    if (mounted && local != null && local.isNotEmpty) {
      setState(() => _url = local);
    }

    // 2) Luego intenta remoto y actualiza cache (no bloquea UI)
    try {
      final remote = await FirebaseService()
          .getPfpUrlByUid(widget.uid)
          .timeout(const Duration(seconds: 10));
      if (!mounted) return;
      if (remote != null && remote.isNotEmpty) {
        await prefs.setString(_cacheKey, remote);
        setState(() => _url = remote);
      }
    } catch (_) {
      // silencioso si no hay internet
    }
  }

  @override
  Widget build(BuildContext context) {
    final ImageProvider provider = (_url != null)
        ? CachedNetworkImageProvider(_url!, cacheKey: _cacheKey)
        : widget.assetFallback;

    return CircleAvatar(
      radius: widget.radius,
      backgroundColor: const Color(0xFFEFEFEF),
      backgroundImage: provider,
    );
  }
}
