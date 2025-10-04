import 'dart:async';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
import '../main.dart'; // Import to access navigatorKey

class ConnectivityService {
  static final ConnectivityService _instance = ConnectivityService._internal();
  factory ConnectivityService() => _instance;
  ConnectivityService._internal();

  final Connectivity _connectivity = Connectivity();
  StreamSubscription<List<ConnectivityResult>>? _connectivitySubscription;
  bool _isOnline = true;
  OverlayEntry? _overlayEntry;

  bool get isOnline => _isOnline;

  void initialize(BuildContext context) {
    // Check initial connectivity status
    _connectivity.checkConnectivity().then((List<ConnectivityResult> results) {
      _updateConnectionStatus(results, context);
    });

    _connectivitySubscription = _connectivity.onConnectivityChanged.listen(
      (List<ConnectivityResult> results) {
        _updateConnectionStatus(results, context);
      },
    );
  }

  void _updateConnectionStatus(
    List<ConnectivityResult> results,
    BuildContext context,
  ) {
    bool hasConnection = results.any(
      (result) =>
          result == ConnectivityResult.mobile ||
          result == ConnectivityResult.wifi ||
          result == ConnectivityResult.ethernet,
    );

    if (_isOnline && !hasConnection) {
      // Lost connection
      _isOnline = false;
      _showNoConnectionOverlay(context);
    } else if (!_isOnline && hasConnection) {
      // Regained connection
      _isOnline = true;
      _hideNoConnectionOverlay();
    } else {}
  }

  void _showNoConnectionOverlay(BuildContext context) {
    if (_overlayEntry != null) return;

    // Get the navigator's overlay using the global navigator key
    final navigatorState = navigatorKey.currentState;
    if (navigatorState == null) {
      return;
    }

    final overlayState = navigatorState.overlay;
    if (overlayState == null) {
      return;
    }

    _overlayEntry = OverlayEntry(
      builder: (context) => Positioned(
        top: MediaQuery.of(context).padding.top + 10,
        left: 20,
        right: 20,
        child: Material(
          color: Colors.transparent,
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: Colors.red.shade600,
              borderRadius: BorderRadius.circular(8),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.3),
                  blurRadius: 10,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Row(
              children: [
                const Icon(
                  Icons.wifi_off,
                  color: Colors.white,
                  size: 20,
                ),
                const SizedBox(width: 12),
                const Expanded(
                  child: Text(
                    'Sin conexión a internet',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
                GestureDetector(
                  onTap: () => _retryConnection(context),
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      border: Border.all(color: Colors.white, width: 1),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: const Text(
                      'Reintentar',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                GestureDetector(
                  onTap: _hideNoConnectionOverlay,
                  child: const Icon(
                    Icons.close,
                    color: Colors.white,
                    size: 18,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );

    overlayState.insert(_overlayEntry!);
  }

  void _hideNoConnectionOverlay() {
    _overlayEntry?.remove();
    _overlayEntry = null;
  }

  Future<void> _retryConnection(BuildContext context) async {
    try {
      // Check current connectivity status
      final List<ConnectivityResult> results = await _connectivity
          .checkConnectivity();

      // Guard against using BuildContext after async gap
      if (!context.mounted) return;

      // Update status based on current connectivity
      _updateConnectionStatus(results, context);

      // If still no connection, show a brief feedback
      if (!_isOnline) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Aún sin conexión. Intenta nuevamente.'),
            duration: Duration(seconds: 2),
            backgroundColor: Colors.orange,
          ),
        );
      }
    } catch (e) {
      // Guard against using BuildContext after async gap
      if (!context.mounted) return;

      // Handle any errors during connectivity check
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Error al verificar conexión.'),
          duration: Duration(seconds: 2),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  void dispose() {
    _connectivitySubscription?.cancel();
    _hideNoConnectionOverlay();
  }
}
