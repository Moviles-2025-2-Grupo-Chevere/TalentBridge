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
}
