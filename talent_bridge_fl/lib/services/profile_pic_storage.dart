import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:path_provider/path_provider.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class ProfileStorage {
  static final _fbService = FirebaseService();

  /// Get the current profile image path
  static Future<String?> getLocalProfileImagePath() async {
    final uid = _fbService.currentUid();
    if (uid == null) {
      throw Exception("Uid not found before saving image");
    }
    final directory = await getApplicationDocumentsDirectory();
    final appPath = '${directory.path}/$uid.jpg';
    final file = File(appPath);
    if (file.existsSync()) {
      return appPath;
    }
    return null;
  }

  /// Save the profile image path
  static Future<String> saveProfilePictureLocally(String tempPath) async {
    final uid = _fbService.currentUid();
    if (uid == null) {
      throw Exception("Uid not found before saving image");
    }
    final localImage = File(tempPath);
    final directory = await getApplicationDocumentsDirectory();
    final appPath = '${directory.path}/$uid.jpg';
    final newFile = await localImage.copy(appPath);
    debugPrint('Profile image path saved: $appPath');
    return newFile.path;
  }

  /// Clear the profile image path
  static void clearProfileImagePath() {
    // In a real app, you would remove this from storage here
    debugPrint('Profile image path cleared');
  }
}
