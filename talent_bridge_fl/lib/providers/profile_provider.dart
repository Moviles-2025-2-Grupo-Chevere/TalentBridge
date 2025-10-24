import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

final profileProvider = NotifierProvider(ProfileNotifier.new);
final _fb = FirebaseService();

class ProfileNotifier extends Notifier<UserEntity?> {
  @override
  build() {
    print('Profile provider rebuilt');
    final asyncProfile = ref.watch(dbProfileProvider);

    // Update state reactively whenever Firestore stream emits
    asyncProfile.whenData((snapshot) {
      if (snapshot.exists) {
        print('User data updated');
        state = UserEntity.fromMap(snapshot.data()!);
      } else {
        print('User data NOT updated');
        state = null;
      }
    });

    return state;
  }
}

final dbProfileProvider = StreamProvider.autoDispose(
  (ref) {
    return _fb.getCurentUserSnapshot()!;
  },
);
