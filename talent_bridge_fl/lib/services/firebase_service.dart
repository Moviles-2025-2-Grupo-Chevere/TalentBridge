import 'dart:io';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_auth/firebase_auth.dart' as firebase_core;
import 'package:firebase_storage/firebase_storage.dart';
import 'package:image_picker/image_picker.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';

class FirebaseService {
  final _auth = FirebaseAuth.instance;
  final _db = FirebaseFirestore.instance;
  final _storage = FirebaseStorage.instance;
  final _analytics = FirebaseAnalytics.instance;

  String? currentUid() => _auth.currentUser?.uid;

  /// Default Firestore user document
  Map<String, dynamic> _defaultUserDoc({
    required String uid,
    required String displayName,
    required String email,
  }) {
    return {
      "id": uid,
      "displayName": displayName,
      "email": email,
      // Defaults
      "headline": "",
      "isPublic": true,
      "linkedin": "",
      "location": "",
      "mobileNumber": "",
      "photoUrl": "",
      "projects": <String>[],
      "skillsOrTopics": <String>[],
      "description": "",
      "major": "",
      "lastPortfolioUpdateAt": "",
      "applications": <String>[],
      "acceptedProjects": <String>[],
    };
  }

  Future<void> _ensureUserDoc({
    required String uid,
    required String displayName,
    required String email,
  }) async {
    final docRef = _db.collection('users').doc(uid);
    final snap = await docRef.get();

    if (!snap.exists) {
      await docRef.set(
        _defaultUserDoc(uid: uid, displayName: displayName, email: email),
        SetOptions(merge: true),
      );
    }
  }

  // ---------------- USER DATA ----------------

  Future<UserEntity?> getCurrentUserEntity() async {
    final uid = currentUid();
    if (uid == null) return null;

    final docRef = _db.collection('users').doc(uid);
    final snap = await docRef.get();

    if (!snap.exists) return null;

    return UserEntity.fromMap(snap.data()!);
  }

  Future<List<UserEntity>> getAllUsers() async {
    final querySnapshot = await _db.collection('users').get();

    return querySnapshot.docs
        .map((doc) => UserEntity.fromMap(doc.data()))
        .toList();
  }

  // ---------------- PROJECTS ----------------
  Future<List<ProjectEntity>> getAllProjects() async {
    final querySnapshot = await _db.collection('users').get();

    List<ProjectEntity> allProjects = [];

    for (var doc in querySnapshot.docs) {
      final userData = doc.data();

      // Check if projects field exists and is a list
      if (userData.containsKey('projects') && userData['projects'] is List) {
        final projectsList = userData['projects'] as List;

        for (var projectData in projectsList) {
          // Skip if not a map
          if (projectData is! Map<String, dynamic>) continue;

          Map<String, dynamic> projectMap = Map<String, dynamic>.from(
            projectData,
          );

          // Ensure createdById exists
          if (!projectMap.containsKey('createdById') ||
              projectMap['createdById'] == null ||
              projectMap['createdById'] == '') {
            projectMap['createdById'] = doc.id;
          }

          try {
            final project = ProjectEntity.fromMap(projectMap);
            allProjects.add(project);
          } catch (e) {
            print('Error parsing project for user ${doc.id}: $e');
          }
        }
      }
    }

    return allProjects;
  }

  Future<void> addProjectToApplications({
    required String userId,
    required String projectId,
  }) async {
    final docRef = _db.collection('users').doc(userId);
    await docRef.update({
      'applications': FieldValue.arrayUnion([projectId]),
    });
  }

  // ---------------- AUTH ----------------

  Future<void> signIn(String email, String pass) async {
    await _auth.signInWithEmailAndPassword(email: email, password: pass);

    final uid = currentUid();
    if (uid != null) {
      await _db.collection('users').doc(uid).set(
        {
          "lastLoginAt": FieldValue.serverTimestamp(),
        },
        SetOptions(merge: true),
      );
    }

    await _analytics.logLogin(loginMethod: 'email');

    await logEvent('login', {});
  }

  Future<void> signOut() => _auth.signOut();

  Future<void> signUp({
    required String email,
    required String password,
    String? displayName,
  }) async {
    final cred = await _auth.createUserWithEmailAndPassword(
      email: email,
      password: password,
    );

    final safeName = (displayName ?? '').trim();
    if (safeName.isNotEmpty) {
      await cred.user?.updateDisplayName(safeName);
    }

    final uid = cred.user!.uid;
    await _ensureUserDoc(
      uid: uid,
      displayName: safeName,
      email: email,
    );

    await _db.collection('users').doc(uid).set(
      {"lastLoginAt": FieldValue.serverTimestamp()},
      SetOptions(merge: true),
    );

    await _analytics.logSignUp(signUpMethod: 'email');

    await logEvent('signup', {});
  }

  // ---------------- PIPELINE ----------------

  Future<void> updateLastLogin() async {
    final uid = currentUid();
    if (uid == null) return;
    await _db.collection('users').doc(uid).set(
      {
        'lastLoginAt': FieldValue.serverTimestamp(),
      },
      SetOptions(merge: true),
    );
  }

  Future<void> updateLastPortfolioUpdate() async {
    final uid = currentUid();
    if (uid == null) return;
    await _db.collection('users').doc(uid).set(
      {
        'lastPortfolioUpdateAt': FieldValue.serverTimestamp(),
      },
      SetOptions(merge: true),
    );

    await _analytics.logEvent(
      name: 'portfolio_update',
      parameters: {'user_id': uid},
    );

    await logEvent('portfolio_update', {});
  }

  // ---------------- EVENTS ----------------

  Future<void> logEvent(String type, Map<String, dynamic> meta) async {
    final uid = currentUid();
    if (uid == null) return;
    await _db.collection('events').add({
      'uid': uid,
      'type': type,
      'ts': FieldValue.serverTimestamp(),
      'meta': meta,
    });
  }

  // ---------- ANALYTICS ----------
  /// Log custom analytics event to Firebase Analytics
  Future<void> logAnalyticsEvent(
    String name,
    Map<String, Object> parameters,
  ) async {
    await _analytics.logEvent(name: name, parameters: parameters);
  }

  /// Set user properties for analytics
  Future<void> setUserProperties({
    required String userId,
    String? userType,
    String? plan,
  }) async {
    await _analytics.setUserId(id: userId);
    if (userType != null) {
      await _analytics.setUserProperty(name: 'user_type', value: userType);
    }
    if (plan != null) {
      await _analytics.setUserProperty(name: 'plan', value: plan);
    }
  }

  Future<TaskSnapshot?> uploadPFP(File image) async {
    var uid = _auth.currentUser?.uid;
    if (uid == null) return null;
    var ref = _storage.ref().child('profile_pictures/$uid');
    try {
      return await ref.putFile(image);
    } on firebase_core.FirebaseException catch (e) {
      // ...
      rethrow;
    } catch (e) {
      rethrow;
    }
  }

  Future<String?> getPFPUrl() async {
    var uid = _auth.currentUser?.uid;
    if (uid == null) return null;
    var ref = _storage.ref().child('profile_pictures/$uid');
    try {
      var url = await ref.getDownloadURL();
      return url;
    } on FirebaseException catch (e) {
      if (e.code == 'object-not-found') {
        print('File does not exist');
        return null;
      }
      rethrow;
    } catch (e) {
      rethrow;
    }
  }
}
