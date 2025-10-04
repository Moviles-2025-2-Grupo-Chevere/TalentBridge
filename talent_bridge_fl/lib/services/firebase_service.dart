import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';

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
