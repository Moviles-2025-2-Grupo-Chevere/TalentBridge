import 'dart:io';
import 'dart:typed_data';
import 'package:archive/archive_io.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/providers/profile_provider.dart';
import 'package:talent_bridge_fl/util/string_utils.dart';
import 'package:pdf/widgets.dart' as pw;

class DownloadProjects extends ConsumerWidget {
  const DownloadProjects({super.key});

  /// Generates PDFs for each project
  static Future<List<Uint8List>> _generateProjectPdfs(
    List<ProjectEntity> projects,
  ) async {
    final List<Future<Uint8List>> pdfFutures = [];

    for (final project in projects) {
      final pdf = pw.Document();

      pdf.addPage(
        pw.Page(
          build: (context) => pw.Column(
            crossAxisAlignment: pw.CrossAxisAlignment.start,
            children: [
              pw.Text(
                project.title,
                style: pw.TextStyle(
                  fontSize: 24,
                  fontWeight: pw.FontWeight.bold,
                ),
              ),
              pw.SizedBox(height: 16),
              if (project.id != null) ...[
                pw.Text('ID: ${project.id}'),
                pw.SizedBox(height: 8),
              ],
              if (project.createdAt != null) ...[
                pw.Text('Created: ${project.createdAt}'),
                pw.SizedBox(height: 8),
              ],
              pw.Text('Created By ID: ${project.createdById}'),
              pw.SizedBox(height: 8),
              if (project.createdBy != null) ...[
                pw.Text(
                  'Created By: ${project.createdBy?.displayName ?? 'Unknown'}',
                ),
                pw.SizedBox(height: 8),
              ],
              pw.Text(
                'Description:',
                style: pw.TextStyle(fontWeight: pw.FontWeight.bold),
              ),
              pw.SizedBox(height: 4),
              pw.Text(project.description),
              pw.SizedBox(height: 16),
              pw.Text(
                'Skills:',
                style: pw.TextStyle(fontWeight: pw.FontWeight.bold),
              ),
              pw.SizedBox(height: 4),
              ...project.skills.map(
                (skill) => pw.Padding(
                  padding: const pw.EdgeInsets.only(left: 8, bottom: 4),
                  child: pw.Text('- $skill'),
                ),
              ),
            ],
          ),
        ),
      );

      pdfFutures.add(pdf.save());
    }

    return await Future.wait(pdfFutures);
  }

  /// Zips PDF files into a single archive
  static Uint8List _zipPdfFiles(
    List<Uint8List> pdfBytes,
    List<ProjectEntity> projects,
  ) {
    final archive = Archive();

    for (int i = 0; i < pdfBytes.length; i++) {
      final project = projects[i];
      // Sanitize filename: remove special characters
      final sanitizedTitle = project.title
          .replaceAll(RegExp(r'[^\w\s-]'), '')
          .trim();
      final fileName = '${project.id ?? i}_$sanitizedTitle.pdf';

      archive.addFile(
        ArchiveFile(fileName, pdfBytes[i].length, pdfBytes[i]),
      );
    }

    final zipBytes = ZipEncoder().encode(archive);
    return Uint8List.fromList(zipBytes);
  }

  /// Main method that generates PDFs and zips them
  static Future<Uint8List> _generateAndZipProjects(
    List<ProjectEntity> projects,
  ) async {
    // Step 1: Generate all PDFs
    final pdfBytes = await _generateProjectPdfs(projects);

    // Step 2: Zip them together
    final zipBytes = _zipPdfFiles(pdfBytes, projects);

    return zipBytes;
  }

  Future<void> _onDownloadFiles(
    BuildContext context,
    WidgetRef ref,
  ) async {
    final user = ref.read(profileProvider);
    final projects = user?.projects ?? [];

    if (projects.isEmpty) return;

    try {
      // Generate and zip in isolate
      final zipBytes = await compute(_generateAndZipProjects, projects);

      // Get Downloads directory for Android
      var directory = Directory('/storage/emulated/0/Download');
      if (!await directory.exists()) {
        directory = (await getExternalStorageDirectory())!;
      }

      // Create timestamped filename
      final timestamp = DateTime.now()
          .toIso8601String()
          .split('.')[0]
          .replaceAll(':', '-')
          .replaceAll('T', '_');
      final file = File('${directory.path}/projects-$timestamp.zip');

      // Save the ZIP file
      await file.writeAsBytes(zipBytes);

      // Show success message
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('${projects.length} projects saved to ${file.path}'),
            duration: const Duration(seconds: 3),
          ),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error saving files: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(profileProvider);
    var projects = user?.projects ?? [];
    return user == null
        ? Center(child: CircularProgressIndicator())
        : Padding(
            padding: const EdgeInsets.all(8.0),
            child: Column(
              children: [
                FilledButton.icon(
                  onPressed: () => _onDownloadFiles(context, ref),
                  label: Text('Download ${projects.length} project files'),
                  icon: Icon(Icons.download),
                ),
                Expanded(
                  child: ListView(
                    children: [
                      ...projects.map(
                        (e) => ListTile(
                          title: Text(capitalize(e.title)),
                          onTap: () {},
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
  }
}
