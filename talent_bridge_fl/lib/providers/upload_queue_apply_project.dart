import 'dart:async';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/providers/project_application.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:hive_flutter/hive_flutter.dart';

final projectApplyUploadProvider =
    NotifierProvider<ProjectApplyUploadNotifier, List<ProjectApplication>>(
      ProjectApplyUploadNotifier.new,
    );

class ProjectApplyUploadNotifier extends Notifier<List<ProjectApplication>> {
  final _connectivity = Connectivity();
  final _fbService = FirebaseService();
  StreamSubscription<List<ConnectivityResult>>? _subscription;

  static const String hiveBoxName = 'project_apply_upload_queue';
  static const String _queueKey = 'pending_applications';
  Box? _hiveBox;

  /// Initialize with empty list and listen to connectivity changes
  @override
  List<ProjectApplication> build() {
    state = [];

    _subscription = _connectivity.onConnectivityChanged.listen((status) {
      if (status[0] != ConnectivityResult.none) {
        // When connection is restored, try uploading queued applications
        if (state.isNotEmpty) {
          processQueue();
        }
      }
    });

    ref.onDispose(() => _subscription?.cancel());
    return state;
  }

  /// Add application to queue and attempt upload
  /// Returns true if uploaded successfully, false if queued for later
  Future<bool> enqueueProjectApplyUpload(
    String userId,
    String projectId,
    String createdById,
  ) async {
    final application = ProjectApplication(
      userId: userId,
      projectId: projectId,
      createdById: createdById,
    );

    // Try to upload immediately
    final success = await tryUploadApplication(application);

    if (success) {
      // Successfully uploaded, no need to queue
      return true;
    } else {
      // Failed to upload, add to queue
      state = [...state, application];
      return false; // null indicates queued for later (like TaskSnapshot?)
    }
  }

  /// Process all queued applications
  Future<void> processQueue() async {
    if (state.isEmpty) return;

    // Create a copy to avoid modification during iteration
    final applicationsToProcess = [...state];

    for (final application in applicationsToProcess) {
      final success = await tryUploadApplication(application);
      if (success) {
        removeFromQueue(application);
      } else {
        // Stop processing if one fails (no connection)
        break;
      }
    }
  }

  /// Attempt to upload a single application
  Future<bool> tryUploadApplication(ProjectApplication app) async {
    try {
      await _fbService
          .addProjectToApplications(
            userId: app.userId,
            createdById: app.createdById,
            projectId: app.projectId,
          )
          .timeout(Duration(seconds: 5));
      return true;
    } catch (e) {
      debugPrint('Failed to upload application: $e');
      return false;
    }
  }

  /// Remove application from queue
  void removeFromQueue(ProjectApplication app) {
    debugPrint('Removing application from queue: $app');
    state = state.where((item) => item != app).toList();
  }
}
