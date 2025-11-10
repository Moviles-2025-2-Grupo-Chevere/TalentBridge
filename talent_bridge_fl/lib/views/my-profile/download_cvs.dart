import 'dart:typed_data';
import 'package:archive/archive_io.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class _DownloadData {
  final List<String> cvUrls;
  const _DownloadData(this.cvUrls);
}

class DownloadCVs {
  /// Downloads a single file from URL
  static Future<Uint8List> _downloadFile(String url) async {
    final startTime = DateTime.now();
    debugPrint('Starting download from: ${url.substring(0, 50)}...');

    final response = await http.get(Uri.parse(url));

    final duration = DateTime.now().difference(startTime);
    debugPrint(
      'Download completed in ${duration.inMilliseconds}ms, size: ${response.bodyBytes.length} bytes, status: ${response.statusCode}',
    );

    if (response.statusCode == 200) {
      return response.bodyBytes;
    } else {
      throw Exception(
        'Failed to download file from $url - Status: ${response.statusCode}',
      );
    }
  }

  /// Downloads CV files from URLs
  static Future<List<Uint8List>> _downloadCVFiles(List<String> cvUrls) async {
    final startTime = DateTime.now();
    debugPrint('Starting parallel download of ${cvUrls.length} files');

    final List<Future<Uint8List>> downloadFutures = [];

    for (final url in cvUrls) {
      downloadFutures.add(_downloadFile(url));
    }

    final results = await Future.wait(downloadFutures);

    final duration = DateTime.now().difference(startTime);
    debugPrint('All downloads completed in ${duration.inMilliseconds}ms');

    return results;
  }

  /// Extracts filename from Firebase Storage URL
  static String _extractFileName(String url, int index) {
    try {
      final uri = Uri.parse(url);
      final pathSegments = uri.pathSegments;

      // Firebase Storage URLs have the filename in the path
      if (pathSegments.length >= 4) {
        final encodedFileName = pathSegments.last;
        final decodedFileName = Uri.decodeComponent(encodedFileName);
        final fileName = decodedFileName.split('/').last;
        return fileName;
      }
    } catch (e) {
      debugPrint('Error extracting filename: $e');
    }

    // Fallback to generic name
    return 'CV_${index + 1}.pdf';
  }

  /// Zips CV files into a single archive
  static Uint8List _zipCVFiles(
    List<Uint8List> cvBytes,
    List<String> cvUrls,
  ) {
    final startTime = DateTime.now();
    debugPrint('Starting zip creation for ${cvBytes.length} files');

    final archive = Archive();

    for (int i = 0; i < cvBytes.length; i++) {
      final fileName = _extractFileName(cvUrls[i], i);
      debugPrint(
        'Adding file to archive: $fileName (${cvBytes[i].length} bytes)',
      );

      archive.addFile(
        ArchiveFile(fileName, cvBytes[i].length, cvBytes[i]),
      );
    }

    final zipBytes = ZipEncoder().encode(archive);

    final duration = DateTime.now().difference(startTime);
    debugPrint(
      'Zip creation completed in ${duration.inMilliseconds}ms, final size: ${zipBytes?.length ?? 0} bytes',
    );

    return Uint8List.fromList(zipBytes!);
  }

  /// Main method that downloads and zips CVs (runs in isolate)
  static Future<Uint8List> _downloadAndZipCVs(_DownloadData data) async {
    final overallStart = DateTime.now();
    debugPrint('=== ISOLATE STARTED ===');
    debugPrint(
      'Downloading ${data.cvUrls.length} CV(s)... at _downloadAndZipCVs',
    );

    // Step 1: Download all CVs
    final cvBytes = await _downloadCVFiles(data.cvUrls);

    // Step 2: Zip them together
    final zipBytes = _zipCVFiles(cvBytes, data.cvUrls);

    final overallDuration = DateTime.now().difference(overallStart);
    debugPrint(
      '=== ISOLATE COMPLETED in ${overallDuration.inMilliseconds}ms ===',
    );

    return zipBytes;
  }

  /// Public method to download all CVs
  static Future<void> downloadAllCVs(
    BuildContext context,
    List<String> cvUrls,
  ) async {
    if (cvUrls.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No CVs to download')),
      );
      return;
    }

    // Store the ScaffoldMessengerState reference before any async operations
    final scaffoldMessenger = ScaffoldMessenger.of(context);

    try {
      final mainStart = DateTime.now();
      debugPrint('=== MAIN: Starting download for ${cvUrls.length} CV(s) ===');
      debugPrint(
        'URLs: ${cvUrls.map((u) => u.substring(0, 60)).join('\n')}...',
      );

      // Show loading snackbar
      scaffoldMessenger.showSnackBar(
        SnackBar(
          content: Row(
            children: [
              const SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                ),
              ),
              const SizedBox(width: 16),
              Text('Downloading ${cvUrls.length} CV(s)...'),
            ],
          ),
          duration: const Duration(hours: 1),
        ),
      );

      debugPrint('MAIN: Spawning isolate with compute()');
      final computeStart = DateTime.now();

      // Download and zip in isolate
      final zipBytes = await compute(
        _downloadAndZipCVs,
        _DownloadData(cvUrls),
      );

      final computeDuration = DateTime.now().difference(computeStart);
      debugPrint(
        'MAIN: compute() returned in ${computeDuration.inMilliseconds}ms, zip size: ${zipBytes.length} bytes',
      );

      // Create timestamped filename
      final timestamp = DateTime.now()
          .toIso8601String()
          .split('.')[0]
          .replaceAll(':', '-')
          .replaceAll('T', '_');

      debugPrint(
        'MAIN: Opening file picker dialog with fileName: CVs-$timestamp.zip',
      );
      final pickerStart = DateTime.now();

      // Save file using FilePicker (same as download_projects)
      // Note: FilePicker doesn't need context, it works independently
      final result = await FilePicker.platform.saveFile(
        dialogTitle: 'Save CVs',
        fileName: 'CVs-$timestamp',
        type: FileType.custom,
        allowedExtensions: ['zip'],
        bytes: zipBytes,
      );

      final pickerDuration = DateTime.now().difference(pickerStart);
      debugPrint(
        'MAIN: File picker completed in ${pickerDuration.inMilliseconds}ms',
      );
      debugPrint('MAIN: Result path: $result');

      // Hide loading snackbar using the stored reference
      debugPrint('MAIN: Hiding loading snackbar');
      scaffoldMessenger.hideCurrentSnackBar();

      if (result == null) {
        debugPrint('MAIN: User cancelled save dialog');
        return;
      }

      final mainDuration = DateTime.now().difference(mainStart);
      debugPrint(
        '=== MAIN: Total operation completed in ${mainDuration.inMilliseconds}ms ===',
      );

      // Show success message using the stored reference
      scaffoldMessenger.showSnackBar(
        SnackBar(
          content: Text('${cvUrls.length} CVs saved'),
          backgroundColor: Colors.green,
          duration: const Duration(seconds: 3),
        ),
      );
    } catch (e, stackTrace) {
      debugPrint('ERROR: $e');
      debugPrint('Stack trace: $stackTrace');

      // Close loading indicator using the stored reference
      scaffoldMessenger.hideCurrentSnackBar();
      scaffoldMessenger.showSnackBar(
        SnackBar(
          content: Text('Error downloading CVs: $e'),
          backgroundColor: Colors.red,
          duration: const Duration(seconds: 3),
        ),
      );
    }
  }
}
