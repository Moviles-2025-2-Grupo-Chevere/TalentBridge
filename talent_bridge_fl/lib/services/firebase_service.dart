import 'dart:io';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_auth/firebase_auth.dart' as firebase_core;
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/domain/update_user_dto.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart' hide Source;

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

  Stream<DocumentSnapshot<Map<String, dynamic>>>? getCurrentUserSnapshot() {
    final uid = currentUid();
    if (uid == null) return null;
    return _db.collection('users').doc(uid).snapshots();
  }

  Stream<UserEntity> userStreamById(String uid) {
    return _db.collection('users').doc(uid).snapshots().map((snap) {
      final data = snap.data() ?? <String, dynamic>{};
      return UserEntity.fromMap({
        ...data,
        'id': snap.id,
      });
    });
  }

  Future<UserEntity?> getCurrentUserEntity(bool? useOnline) async {
    final uid = currentUid();
    if (uid == null) return null;

    final docRef = _db.collection('users').doc(uid);
    final snap = await docRef.get(
      GetOptions(
        source: useOnline == true ? Source.server : Source.serverAndCache,
      ),
    );

    if (!snap.exists) return null;

    return UserEntity.fromMap(snap.data()!);
  }

  Future<UserEntity> getUserById(String uid) async {
    final snap = await _db.collection('users').doc(uid).get();
    if (!snap.exists) throw Exception('User $uid not found');
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
    try {
      final querySnapshot = await _db.collection('users').get();

      List<ProjectEntity> allProjects = [];

      for (var doc in querySnapshot.docs) {
        final userData = doc.data();
        final UserEntity userEntity = UserEntity.fromMap(userData);
        final UserEntity userWoutProjects = userEntity.copyWith(projects: null);
        final userProjects = userEntity.projects ?? [];
        final projectsWithUser = userProjects.map(
          (e) {
            e.createdBy = userWoutProjects;
            return e;
          },
        ).toList();
        allProjects.addAll(projectsWithUser);
      }
      allProjects.sort((a, b) {
        final aDate = a.createdAt ?? DateTime(1970);
        final bDate = b.createdAt ?? DateTime(1970);
        return -aDate.compareTo(bDate);
      });

      return allProjects;
    } catch (e) {
      print("Error mapping projects: " + e.toString());
      rethrow;
    }
  }

  Future<void> addProjectToApplications({
    required String userId,
    required String createdById,
    required String projectId,
  }) async {
    final docRef = _db.collection('users').doc(userId);
    await docRef.update({
      'applications': FieldValue.arrayUnion([
        {"projectId": projectId, "createdById": createdById},
      ]),
    });

    // Get user major
    final userEntity = await getCurrentUserEntity(false);
    final major = userEntity?.major ?? '';
    debugPrint('User major: $major');

    // Use a composite ID: userId_projectId to prevent duplicates
    final applicationId = '${userId}_$projectId';

    // Use .set() with merge instead of .add()
    await _db.collection('projectApplications').doc(applicationId).set({
      'appliedDate': FieldValue.serverTimestamp(),
      'project_id': projectId,
      'user_id': userId,
      'major': major == '' ? 'Undeclared' : major,
    }, SetOptions(merge: true)); // merge:true updates if exists, creates if not

    // Log analytics
    final timestamp = DateTime.now().millisecondsSinceEpoch;
    await _analytics.logEvent(
      name: 'application_to_project',
      parameters: {
        'timestamp': timestamp,
        'user_id': userId,
        'project_id': projectId,
        'major': major == '' ? 'Undeclared' : major,
      },
    );
  }

  Future<List<Map<String, String>>> getUsersWhoAppliedToProject({
    required String projectId,
  }) async {
    try {
      // Query for users where applications array contains the projectId
      final querySnapshot = await _db
          .collection('users')
          .where('applications', arrayContains: projectId)
          .get();

      // Map results to a list of user IDs and names
      List<Map<String, String>> applicants = [];

      for (var doc in querySnapshot.docs) {
        final userData = doc.data();
        applicants.add({
          'id': doc.id,
          'name': userData['displayName'] ?? 'Unnamed User',
        });
      }

      return applicants;
    } catch (e) {
      print('Error getting applicants for project $projectId: $e');
      return [];
    }
  }

  /// Accept a user's application to a project
  Future<void> acceptProject({
    required String userId,
    required String projectId,
  }) async {
    try {
      // First, get the project to retrieve its skills
      ProjectEntity? project;
      final projects = await getAllProjects();
      project = projects.firstWhere(
        (p) => p.id == projectId,
        orElse: () => throw Exception('Project not found'),
      );

      // 1. Update user document - remove from applications, add to acceptedProjects
      final userRef = _db.collection('users').doc(userId);
      await userRef.update({
        'applications': FieldValue.arrayRemove([projectId]),
        'acceptedProjects': FieldValue.arrayUnion([projectId]),
      });

      // 2. Create new document in acceptedProjects collection
      await _db.collection('acceptedProjects').add({
        'acceptedDate': FieldValue.serverTimestamp(),
        'project_id': projectId,
        'user_id': userId,
        'skills': project.skills,
      });

      // 3. Log analytics event
      final timestamp = DateTime.now().millisecondsSinceEpoch;
      await _analytics.logEvent(
        name: 'project_accepted',
        parameters: {
          'timestamp': timestamp,
          'user_id': userId,
          'project_id': projectId,
          'skills': project.skills.join(
            ',',
          ), // Convert list to string for analytics
        },
      );

      // 4. Log to events collection
      await logEvent('project_accepted', {
        'project_id': projectId,
        'user_id': userId,
        'skills': project.skills,
      });
    } catch (e) {
      print('Error accepting project: $e');
      rethrow;
    }
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
      sendFCMToken();
    }

    await _analytics.logLogin(loginMethod: 'email');

    await logEvent('login', {});
  }

  Future<void> signOut() async {
    _auth.signOut();
    _analytics.logEvent(
      name: 'sign_out',
      parameters: {'uid': currentUid() ?? '?'},
    );
  }

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

  Future<bool> sendFCMToken() async {
    try {
      final uid = currentUid();
      if (uid == null) return false;
      var messaging = FirebaseMessaging.instance;
      final token = await messaging.getToken();
      if (token == null) return false;
      await _db.collection('users').doc(uid).set(
        {"fcm": token},
        SetOptions(merge: true),
      );
      return true;
    } catch (e) {
      print(e);
      return false;
    }
  }

  Future<void> setupNotifications() async {
    var messaging = FirebaseMessaging.instance;
    final notificationSettings = await messaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
      provisional: true,
    );
    await sendFCMToken();
    // print("Token: $token");
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

  Future<TaskSnapshot?> uploadPFP(File image) async {
    var uid = _auth.currentUser?.uid;
    if (uid == null) return null;
    final connection = await Connectivity().checkConnectivity();
    if (connection[0] == ConnectivityResult.none) {
      print('No internet connection, skipping upload.');
      return null;
    }
    var ref = _storage.ref().child('profile_pictures/$uid');
    try {
      final task = await ref.putFile(image);
      return task;
    } on firebase_core.FirebaseException catch (e) {
      print('Firebase error: ${e.code} - ${e.message}');
      if (e.code == 'retry-limit-exceeded') {
        print('No internet connection.');
        return null;
      } else {
        rethrow;
      }
    } catch (e) {
      rethrow;
    }
  }

  // ---------- CV Upload and View ----------
  /// Upload multiple CV files for a user
  Future<List<TaskSnapshot>> uploadMultipleCVs(List<File> files) async {
    final uid = _auth.currentUser?.uid;
    if (uid == null) return [];

    // Create a list of futures for all uploads
    final List<Future<TaskSnapshot>> uploadFutures = [];

    // Create unique filenames for each document
    for (int i = 0; i < files.length; i++) {
      final File file = files[i];
      final filename = '${DateTime.now().millisecondsSinceEpoch}_$i.pdf';
      final ref = _storage.ref().child('cv/$uid/$filename');

      // Add the upload task to our list
      uploadFutures.add(ref.putFile(file));
    }

    try {
      // Wait for all uploads to complete in parallel
      final results = await Future.wait(uploadFutures);

      // Log the upload event
      await logEvent('cv_upload', {
        'count': files.length,
        'timestamp': DateTime.now().toIso8601String(),
      });

      return results;
    } catch (e) {
      print('Error uploading CVs: $e');
      rethrow;
    }
  }

  // Get a list of all CV download URLs for the current user
  Future<List<String>> getCVUrls() async {
    final uid = _auth.currentUser?.uid;
    if (uid == null) return [];

    try {
      final ListResult result = await _storage.ref().child('cv/$uid').listAll();

      // Get download URLs for all items
      final List<Future<String>> urlFutures = result.items
          .map((ref) => ref.getDownloadURL())
          .toList();
      return await Future.wait(urlFutures);
    } on FirebaseException catch (e) {
      if (e.code == 'object-not-found') {
        print('No CVs found for user');
        return [];
      }
      rethrow;
    } catch (e) {
      print('Error getting CV URLs: $e');
      return [];
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

  Future<void> updateUserProfile(UpdateUserDto data) async {
    final uid = currentUid();
    if (uid == null) throw Exception("Uid not set");
    try {
      await _db.collection('users').doc(uid).update(data.toMap());
      return;
    } on FirebaseException catch (e) {
      debugPrint(e.code);
      rethrow;
    } catch (e) {
      rethrow;
    }
  }
}
