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
    // final asyncProfile = ref.watch(remoteProfileProvider);

    // // Update state reactively whenever Firestore stream emits
    // asyncProfile.whenData((snapshot) {
    //   if (snapshot.exists) {
    //     print('User data updated');
    //     UserEntity userEntity;
    //     try {
    //       userEntity = UserEntity.fromMap(snapshot.data()!);
    //       state = userEntity;
    //     } catch (e) {
    //       print(e);
    //     }
    //   } else {
    //     print('User data NOT updated');
    //     state = null;
    //   }
    // });

    return state;
  }

  updateLocalWithOnline() async {
    final remoteValue = await _fb.getCurrentUserEntity(true);
    if (remoteValue != null) {
      await _db.saveProfileLocally(remoteValue);
      state = remoteValue;
    }
  }
}

final remoteProfileProvider = StreamProvider.autoDispose(
  (ref) {
    return _fb.getCurrentUserSnapshot()!;
  },
);
