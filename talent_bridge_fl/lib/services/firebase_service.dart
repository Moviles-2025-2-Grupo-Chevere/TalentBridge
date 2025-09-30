import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
// import 'package:firebase_performance/firebase_performance.dart';

class FirebaseService {
  final _auth = FirebaseAuth.instance;
  final _db = FirebaseFirestore.instance;
  // final _perf = FirebasePerformance.instance; // (luego)
  // final Map<String, dynamic> _traces = {};    // (luego)

  // ---------- AUTH ----------
  Future<void> signIn(String email, String pass) async {
    await _auth.signInWithEmailAndPassword(email: email, password: pass);
    await updateLastLogin();
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
    if (displayName != null && displayName.trim().isNotEmpty) {
      await cred.user?.updateDisplayName(displayName.trim());
    }

    final uid = currentUid();
    if (uid != null) {
      await _db.collection('users').doc(uid).set({
        'displayName': displayName,
        'createdAt': FieldValue.serverTimestamp(),
      }, SetOptions(merge: true));
    }

    await updateLastLogin();
    await logEvent('signup', {});
  }

  String? currentUid() => _auth.currentUser?.uid;

  // ---------- PIPELINE ---------
  Future<void> updateLastLogin() async {
    final uid = currentUid();
    if (uid == null) return;
    await _db.collection('users').doc(uid).set(
      {'lastLoginAt': FieldValue.serverTimestamp()},
      SetOptions(merge: true),
    );
  }

  Future<void> updateLastPortfolioUpdate() async {
    final uid = currentUid();
    if (uid == null) return;
    await _db.collection('users').doc(uid).set(
      {'lastPortfolioUpdateAt': FieldValue.serverTimestamp()},
      SetOptions(merge: true),
    );
    await logEvent('portfolio_update', {});
  }

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

  // ---------- Performance (descomenta cuando agregues firebase_performance) ----------
  // Future<void> startTrace(String name) async {
  //   final t = _perf.newTrace(name);
  //   await t.start();
  //   _traces[name] = t;
  // }
  // Future<void> stopTrace(String name) async {
  //   final t = _traces.remove(name);
  //   await (t as Trace?)?.stop();
  // }
}
