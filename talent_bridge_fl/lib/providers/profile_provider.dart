import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/services/db_service.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

final profileProvider = NotifierProvider(ProfileNotifier.new);
final _fb = FirebaseService();
final _db = DbService();

class ProfileNotifier extends Notifier<UserEntity?> {
  @override
  build() {
    print('Profile provider rebuilt');
    state = null;
    updateLocalWithOnline();
    return state;
  }

  updateLocalWithOnline() async {
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
