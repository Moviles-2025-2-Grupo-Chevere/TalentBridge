import 'dart:typed_data';
import 'package:archive/archive_io.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class DownloadCVs {
  // Download a single file from firebase url
  static Future<Uint8List> _downloadFile(String url) async {
    final response = await http.get(Uri.parse(url));
    if (response.statusCode == 200) {
      return response.bodyBytes;
    } else {
      throw Exception('Failed to download file from $url');
    }
  }

  // Download multiple CV files given their URLs
  static Future<List<Uint8List>> _downloadCVFiles(List<String> cvUrls) async {
    final List<Future<Uint8List>> downloadFutures = [];

    for (final url in cvUrls) {
      downloadFutures.add(_downloadFile(url));
    }

    return await Future.wait(downloadFutures);
  }

  // Extracts filename from Firebase Storage URL
  static String _extractFileName(String url, int index) {
    try {
      final uri = Uri.parse(url);
      final pathSegments = uri.pathSegments;

      // Firebase Storage URLs have the filename in the path
      // Format: /v0/b/{bucket}/o/{path}
      if (pathSegments.length >= 4) {
        final encodedFileName = pathSegments.last;
        // Decode URL encoding
        final decodedFileName = Uri.decodeComponent(encodedFileName);
        // Extract just the filename after the last '/'
        final fileName = decodedFileName.split('/').last;
        return fileName;
      }
    } catch (e) {
      debugPrint('Error extracting filename: $e');
    }

    // Fallback to generic name
    return 'CV_${index + 1}.pdf';
  }

  // Zips CV files into a single archive
  static Uint8List _zipCVFiles(
    List<Uint8List> cvBytes,
    List<String> cvUrls,
  ) {
    final archive = Archive();

    for (int i = 0; i < cvBytes.length; i++) {
      // Extract filename from URL or use index
      final fileName = _extractFileName(cvUrls[i], i);

      archive.addFile(
        ArchiveFile(fileName, cvBytes[i].length, cvBytes[i]),
      );
    }

    final zipBytes = ZipEncoder().encode(archive);
    return Uint8List.fromList(zipBytes!);
  }
}
