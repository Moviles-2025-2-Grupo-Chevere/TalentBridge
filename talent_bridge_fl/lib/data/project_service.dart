import 'dart:convert';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class ProjectService {
  final _db = FirebaseFirestore.instance;
  final _analytics = FirebaseAnalytics.instance;
  final _firebaseService = FirebaseService();

  createProject(ProjectEntity project) async {
    final docRef = FirebaseFirestore.instance
        .collection("users")
        .doc(project.createdById);
    await docRef.update({
      'projects': FieldValue.arrayUnion([project.toMap()]),
    });
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
