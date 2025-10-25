import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/services/db_service.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

final profileProvider = NotifierProvider(ProfileNotifier.new);
final _fb = FirebaseService();
final _db = DbService();

class ProfileNotifier extends Notifier<UserEntity?> {
  StreamSubscription<List<ConnectivityResult>>? _connectivitySubscription;
  StreamSubscription<DocumentSnapshot<Map<String, dynamic>>>?
  _updateSuscription;
  final _connectivity = Connectivity();

  @override
  build() {
    print('Profile provider rebuilt');
    state = null;
    _connectivitySubscription = _connectivity.onConnectivityChanged.listen((
      status,
    ) {
      if (status[0] != ConnectivityResult.none) {
        print("Connection recovered, refreshing profile data");
        _updateLocalWithOnline();
      }
    });

    _updateSuscription = _fb.getCurrentUserSnapshot()?.listen(
      (event) {
        print("Change in data, refreshing profile data");
        _updateLocalWithOnline();
      },
    );

    ref.onDispose(() => _connectivitySubscription?.cancel());
    ref.onDispose(() => _updateSuscription?.cancel());
    _updateLocalWithOnline();
    return state;
  }

  _updateLocalWithOnline() async {
    final remoteValue = await _fb
        .getCurrentUserEntity(true)
        .catchError((e) => null);
    if (remoteValue != null) {
      await _db.saveProfileLocally(remoteValue);
      state = remoteValue;
    } else {
      // fallback to online
      state = await _db.getProfileLocally();
    }
  }
}

final remoteProfileProvider = StreamProvider.autoDispose(
  (ref) {
    return _fb.getCurrentUserSnapshot()!;
  },
);
