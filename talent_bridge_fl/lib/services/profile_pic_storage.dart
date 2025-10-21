import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:path_provider/path_provider.dart';

class ProfileStorage {
  static String? _profileImagePath;

  /// Get the current profile image path
  static Future<String?> getLocalProfileImagePath() async {
    final directory = await getApplicationDocumentsDirectory();
    final appPath = '${directory.path}/profile_picture.jpg';
    final file = File(appPath);
    if (file.existsSync()) {
      return appPath;
    }
    return null;
  }

  /// Save the profile image path
  static Future<String> saveProfilePictureLocally(String tempPath) async {
    final localImage = File(tempPath);
    final directory = await getApplicationDocumentsDirectory();
    final appPath = '${directory.path}/profile_picture.jpg';
    final newFile = await localImage.copy(appPath);
    debugPrint('Profile image path saved: $tempPath');
    return newFile.path;
  }

  /// Clear the profile image path
  static void clearProfileImagePath() {
    _profileImagePath = null;
    // In a real app, you would remove this from storage here
    debugPrint('Profile image path cleared');
  }
}
