import 'dart:async';
import 'dart:io';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/providers/cv_upload.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:firebase_storage/firebase_storage.dart';

final cvUploadProvider = NotifierProvider<CVUploadNotifier, List<CVUpload>>(
  CVUploadNotifier.new,
);

class CVUploadNotifier extends Notifier<List<CVUpload>> {
  final _connectivity = Connectivity();
  final _fbService = FirebaseService();
  StreamSubscription<List<ConnectivityResult>>? _subscription;

  static const String hiveBoxName = 'cv_upload_queue';
  static const String _queueKey = 'pending_cv_uploads';
  Box? _box;

  @override
  List<CVUpload> build() {
    // Initialize with empty list first
    _initializeHive();

    _subscription = _connectivity.onConnectivityChanged.listen((status) {
      if (status[0] != ConnectivityResult.none) {
        // When connection is restored, try uploading queued CVs
        if (state.isNotEmpty) {
          processQueue();
        }
      }
    });

    ref.onDispose(() {
      _subscription?.cancel();
      _box?.close();
    });

    // Return empty list initially
    return [];
  }

  Future<void> _initializeHive() async {
    try {
      _box = await Hive.openBox(hiveBoxName);
      await _loadQueueFromHive();
    } catch (e) {
      debugPrint('Error initializing CV upload Hive box: $e');
      state = [];
    }
  }

  Future<void> _loadQueueFromHive() async {
    final storedData = _box?.get(_queueKey);

    if (storedData != null && storedData is List) {
      state = storedData
          .map(
            (item) => CVUpload.fromMap(Map<String, dynamic>.from(item)),
          )
          .toList();

      debugPrint('Loaded ${state.length} CV uploads from Hive');

      // Try to process queue immediately if there's connectivity
      final connectivityResult = await _connectivity.checkConnectivity();
      if (connectivityResult[0] != ConnectivityResult.none &&
          state.isNotEmpty) {
        processQueue();
      }
    } else {
      state = [];
    }
  }

  Future<void> _saveQueueToHive() async {
    try {
      final dataToStore = state.map((cv) => cv.toMap()).toList();
      await _box?.put(_queueKey, dataToStore);
      debugPrint('Saved ${state.length} CV uploads to Hive');
    } catch (e) {
      debugPrint('Error saving CV queue to Hive: $e');
    }
  }

  /// Add CV uploads to queue and attempt immediate upload
  /// Returns list of TaskSnapshots for successful uploads, or empty list if queued
  Future<List<TaskSnapshot>> enqueueCVUploads(List<File> files) async {
    final uploads = files.map((file) {
      return CVUpload(
        localPath: file.path,
        fileName: file.path.split('/').last,
        queuedAt: DateTime.now(),
      );
    }).toList();

    // Try to upload immediately
    final results = await tryUploadCVs(uploads);

    if (results.isNotEmpty) {
      // Successfully uploaded all files
      return results;
    } else {
      // Failed to upload, add to queue
      state = [...state, ...uploads];
      await _saveQueueToHive();
      debugPrint('${uploads.length} CVs queued for later upload');
      return [];
    }
  }

  /// Process all queued CV uploads
  Future<void> processQueue() async {
    if (state.isEmpty) return;

    debugPrint('Processing CV upload queue with ${state.length} files');

    // Create a copy to avoid modification during iteration
    final uploadsToProcess = [...state];

    for (final upload in uploadsToProcess) {
      // Check if file still exists
      final file = File(upload.localPath);
      if (!await file.exists()) {
        debugPrint('File no longer exists: ${upload.localPath}');
        await removeFromQueue(upload);
        continue;
      }

      final success = await tryUploadSingleCV(upload);
      if (success) {
        await removeFromQueue(upload);
      } else {
        // Stop processing if one fails (no connection)
        break;
      }
    }
  }

  /// Attempt to upload multiple CVs
  Future<List<TaskSnapshot>> tryUploadCVs(List<CVUpload> uploads) async {
    try {
      final files = <File>[];
      for (final upload in uploads) {
        final file = File(upload.localPath);
        if (await file.exists()) {
          files.add(file);
        } else {
          debugPrint('File not found: ${upload.localPath}');
        }
      }

      if (files.isEmpty) return [];

      final results = await _fbService
          .uploadMultipleCVs(files)
          .timeout(const Duration(seconds: 30));

      debugPrint('Successfully uploaded ${results.length} CVs');
      return results;
    } catch (e) {
      debugPrint('Failed to upload CVs: $e');
      return [];
    }
  }

  /// Attempt to upload a single CV
  Future<bool> tryUploadSingleCV(CVUpload upload) async {
    try {
      final file = File(upload.localPath);
      if (!await file.exists()) {
        debugPrint('File not found: ${upload.localPath}');
        return false;
      }

      await _fbService
          .uploadMultipleCVs([file])
          .timeout(const Duration(seconds: 10));

      debugPrint('Successfully uploaded CV: ${upload.fileName}');
      return true;
    } catch (e) {
      debugPrint('Failed to upload CV ${upload.fileName}: $e');
      return false;
    }
  }

  /// Remove CV upload from queue
  Future<void> removeFromQueue(CVUpload upload) async {
    debugPrint('Removing CV from queue: ${upload.fileName}');
    state = state.where((item) => item != upload).toList();
    await _saveQueueToHive();
  }

  /// Get count of pending uploads
  int get pendingCount => state.length;
}
