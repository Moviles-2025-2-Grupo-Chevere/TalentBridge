import 'package:talent_bridge_fl/services/firebase_service.dart';

class ScreenTimer {
  final String eventName; // p.ej. 'first_content_home'
  final Map<String, Object?> base; // {screen: 'Home'} etc.
  final _sw = Stopwatch();
  bool _ended = false;

  ScreenTimer(this.eventName, {Map<String, Object?>? baseParams})
    : base = baseParams ?? {} {
    _sw.start();
  }

  /// Llama UNA sola vez cuando el primer contenido real aparece en pantalla.
  Future<void> endOnce({
    String? source, // 'cache' | 'network' | 'unknown'
    int? itemCount, // # de ítems en ese primer render (si aplica)
    Map<String, Object?> extra = const {},
  }) async {
    if (_ended) return;
    _ended = true;
    _sw.stop();
    final ms = _sw.elapsedMilliseconds;

    final params = <String, Object?>{
      ...base,
      ...extra,
      'duration_ms': ms,
      if (source != null) 'source': source,
      if (itemCount != null) 'item_count': itemCount,
      'ts': DateTime.now().toIso8601String(),
    };

    await FirebaseService().logAnalyticsEvent(eventName, _sanitize(params));
  }

  Map<String, Object> _sanitize(Map<String, Object?> raw) {
    final out = <String, Object>{};
    raw.forEach((k, v) {
      if (v == null) return;
      if (v is num || v is String || v is bool) {
        out[k] = v;
      } else {
        out[k] = v.toString();
      }
    });
    return out;
  }
}
