import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class UserService {
  final _firebaseService = FirebaseService();

  Future<List<UserEntity>> getUsersFromFb() async {
    // This would normally fetch from a backend or database
    print("Fetching users from Firebase...");
    final users = await _firebaseService.getAllUsers();
    print('Users retrieved from Firebase:');
    print('Number of users: ${users.length}');

    // Print details of each user
    for (var i = 0; i < users.length; i++) {
      print('User $i: ${users[i].displayName} (ID: ${users[i].id})');
    }
    return users;
  }
}
