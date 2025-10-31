import 'dart:typed_data';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/providers/profile_provider.dart';
import 'package:talent_bridge_fl/util/string_utils.dart';
import 'package:pdf/widgets.dart' as pw;

class DownloadProjects extends ConsumerWidget {
  const DownloadProjects({super.key});

  Future<List<Uint8List>> _generateProjectPdfs(
    List<ProjectEntity> projects,
  ) async {
    return await compute(_generatePdfsInIsolate, projects);
  }

  static Future<List<Uint8List>> _generatePdfsInIsolate(
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
                  child: pw.Text('• $skill'),
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

  Future _onDownloadFiles() async {}

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
                  onPressed: _onDownloadFiles,
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
