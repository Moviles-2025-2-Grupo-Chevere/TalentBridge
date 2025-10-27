import 'dart:convert';
import 'dart:io';

import 'package:firebase_auth/firebase_auth.dart' as firebase_core;
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class ProjectService {
  final _db = FirebaseFirestore.instance;
  final _st = FirebaseStorage.instance;
  final _analytics = FirebaseAnalytics.instance;
  final _firebaseService = FirebaseService();

  Future<void> createProject(ProjectEntity project, String? imagePath) async {
    final docRef = FirebaseFirestore.instance
        .collection("users")
        .doc(project.createdById);
    await docRef.update({
      'projects': FieldValue.arrayUnion([project.toMap()]),
    });
    if (imagePath != null) {
      var ref = _st.ref().child('project_images/${project.id}');
      try {
        await ref.putFile(File(imagePath));
      } on firebase_core.FirebaseException catch (e) {
        print('Firebase error: ${e.code} - ${e.message}');
        if (e.code == 'retry-limit-exceeded') {
          print('No internet connection.');
        } else {
          rethrow;
        }
      } catch (e) {
        rethrow;
      }
    }
    await _analytics.logEvent(
      name: "create_project",
      parameters: {
        "createdById": project.createdById,
        "title": project.title,
        "skills": jsonEncode(project.skills),
        "id": project.id ?? "",
      },
    );
  }
}
