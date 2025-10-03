import 'package:flutter_test/flutter_test.dart';
import 'package:talent_bridge_fl/services/profile_storage.dart';

void main() {
  group('ProfileStorage Tests', () {
    test('should save and retrieve profile image path', () {
      const testPath = '/path/to/test/image.jpg';
      
      // Save the path
      ProfileStorage.saveProfileImagePath(testPath);
      
      // Retrieve the path
      final retrievedPath = ProfileStorage.getProfileImagePath();
      
      // Verify the path was saved correctly
      expect(retrievedPath, equals(testPath));
    });

    test('should handle null path', () {
      // Save null path
      ProfileStorage.saveProfileImagePath(null);
      
      // Retrieve the path
      final retrievedPath = ProfileStorage.getProfileImagePath();
      
      // Verify null was saved correctly
      expect(retrievedPath, isNull);
    });

    test('should clear profile image path', () {
      const testPath = '/path/to/test/image.jpg';
      
      // Save a path first
      ProfileStorage.saveProfileImagePath(testPath);
      expect(ProfileStorage.getProfileImagePath(), equals(testPath));
      
      // Clear the path
      ProfileStorage.clearProfileImagePath();
      
      // Verify path was cleared
      expect(ProfileStorage.getProfileImagePath(), isNull);
    });
  });
}