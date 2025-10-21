import 'dart:async';
import 'dart:io';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

final pfpUploadProvider = NotifierProvider(PfpUploadNotifier.new);

class PfpUploadNotifier extends Notifier<String?> {
  final _connectivity = Connectivity();
  final _fbService = FirebaseService();
  StreamSubscription<List<ConnectivityResult>>? _subscription;

  @override
  String? build() {
    state = null;

    _subscription = _connectivity.onConnectivityChanged.listen((status) {
      if (status[0] != ConnectivityResult.none) {
        if (state != null) {
          tryUpload(state!);
        }
      }
    });

    ref.onDispose(() => _subscription?.cancel());
    return state;
  }

  Future<TaskSnapshot?> enqueuePfpUpload(String path) async {
    state = path;
    return await tryUpload(path);
  }

  Future<TaskSnapshot?> tryUpload(String path) async {
    final file = File(path);
    final uploadResult = await _fbService.uploadPFP(file);
    if (uploadResult != null) {
      state = null;
    }
    return uploadResult;
  }
}
