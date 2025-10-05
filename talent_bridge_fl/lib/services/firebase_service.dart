import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';

class UserDocument {
  final String id;
  final String displayName;
  final String email;
  final String headline;
  final bool isPublic;
  final String linkedin;
  final String location;
  final String mobileNumber;
  final String photoUrl;
  final List<String> projects;
  final List<String> skillsOrTopics;
  final String description;
  final String major;
  final String lastPortfolioUpdateAt;

  UserDocument({
    required this.id,
    required this.displayName,
    required this.email,
    required this.headline,
    required this.isPublic,
    required this.linkedin,
    required this.location,
    required this.mobileNumber,
    required this.photoUrl,
    required this.projects,
    required this.skillsOrTopics,
    required this.description,
    required this.major,
    required this.lastPortfolioUpdateAt,
  });

  factory UserDocument.fromMap(Map<String, dynamic> map) {
    return UserDocument(
      id: map['id'] ?? '',
      displayName: map['displayName'] ?? '',
      email: map['email'] ?? '',
      headline: map['headline'] ?? '',
      isPublic: map['isPublic'] ?? true,
      linkedin: map['linkedin'] ?? '',
      location: map['location'] ?? '',
      mobileNumber: map['mobileNumber'] ?? '',
      photoUrl: map['photoUrl'] ?? '',
      projects: List<String>.from(map['projects'] ?? []),
      skillsOrTopics: List<String>.from(map['skillsOrTopics'] ?? []),
      description: map['description'] ?? '',
      major: map['major'] ?? '',
      lastPortfolioUpdateAt: map['lastPortfolioUpdateAt'] ?? '',
    );
  }
}

class FirebaseService {
  final _auth = FirebaseAuth.instance;
  final _db = FirebaseFirestore.instance;

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

  Future<UserDocument?> getCurrentUserDocument() async {
    final uid = currentUid();
    if (uid == null) return null;

    final docRef = _db.collection('users').doc(uid);
    final snap = await docRef.get();

    if (!snap.exists) return null;

    return UserDocument.fromMap(snap.data()!);
  }

  Future<List<UserDocument>> getAllUsers() async {
    final querySnapshot = await _db.collection('users').get();

    return querySnapshot.docs
        .map((doc) => UserDocument.fromMap(doc.data()))
        .toList();
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
      displayName: safeName.isEmpty ? "" : safeName,
      email: email,
    );

    await _db.collection('users').doc(uid).set(
      {"lastLoginAt": FieldValue.serverTimestamp()},
      SetOptions(merge: true),
    );

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
}
