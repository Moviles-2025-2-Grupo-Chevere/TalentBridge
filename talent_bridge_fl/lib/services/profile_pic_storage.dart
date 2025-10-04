import 'package:flutter/foundation.dart';

class ProfileStorage {
  static String? _profileImagePath;

  /// Get the current profile image path
  static String? getProfileImagePath() {
    return _profileImagePath;
  }

  /// Save the profile image path
  static void saveProfileImagePath(String? path) {
    _profileImagePath = path;
    // Might persist this to storage here -> Add to firebase
    debugPrint('Profile image path saved: $path');
  }

  /// Clear the profile image path
  static void clearProfileImagePath() {
    _profileImagePath = null;
    // In a real app, you would remove this from storage here
    debugPrint('Profile image path cleared');
  }
}
