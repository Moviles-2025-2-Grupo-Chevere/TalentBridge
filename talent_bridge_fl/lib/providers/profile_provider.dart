import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/services/db_service.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

final profileProvider = NotifierProvider(ProfileNotifier.new);
final _fb = FirebaseService();
final _auth = FirebaseAuth.instance;
final _db = DbService();

class ProfileNotifier extends Notifier<UserEntity?> {
  StreamSubscription<List<ConnectivityResult>>? _connectivitySubscription;
  StreamSubscription<DocumentSnapshot<Map<String, dynamic>>>?
  _updateSuscription;
  StreamSubscription<User?>? _authSuscription;
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

    // Fix -- breaks on user change
    _updateSuscription = _fb.getCurrentUserSnapshot()?.listen(
      (event) {
        print("Change in data, refreshing profile data");
        _updateLocalWithOnline();
      },
    );

    _authSuscription = _auth.authStateChanges().listen(
      (event) async {
        print("Change in auth data, refreshing profile data");
        if (event == null) {
          state = null;
        } else {
          await _updateSuscription?.cancel();
          await _updateLocalWithOnline();
          print("re-setting update listener");
          _updateSuscription = _fb.getCurrentUserSnapshot()?.listen(
            (event) {
              print("Change in data, refreshing profile data");
              _updateLocalWithOnline();
            },
          );
        }
      },
    );
    ref.onDispose(() => _connectivitySubscription?.cancel());
    ref.onDispose(() => _updateSuscription?.cancel());
    ref.onDispose(() => _authSuscription?.cancel());
    _updateLocalWithOnline();
    return state;
  }

  Future _updateLocalWithOnline() async {
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
