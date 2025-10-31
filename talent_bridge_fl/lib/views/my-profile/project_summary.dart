import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class ProjectSummary extends StatelessWidget {
  ProjectSummary({
    super.key,
    required this.project,
  });

  final ProjectEntity project;
  final firebaseService = FirebaseService();

  Future<void> acceptApplicant(
    BuildContext context,
    Map<String, String> applicant,
  ) async {
    try {
      // Show loading indicator
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(
        SnackBar(
          content: Text(
            'Accepting application...',
          ),
        ),
      );

      await firebaseService.acceptProject(
        userId: applicant['id'] ?? '',
        projectId: project.id ?? '',
      );

      if (context.mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(
          SnackBar(
            content: Text(
              'Application accepted successfully!',
            ),
          ),
        );
        Navigator.of(context).pop();
      }
    } catch (e) {
      // Show error message
      if (context.mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(
          SnackBar(
            content: Text(
              'Error accepting application: $e',
            ),
          ),
        );
      }
    }
  }

  showApplicantDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return Dialog(
          child: FutureBuilder<List<Map<String, String>>>(
            future: firebaseService.getUsersWhoAppliedToProject(
              projectId: project.id ?? '',
            ),
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const Padding(
                  padding: EdgeInsets.all(20.0),
                  child: Center(child: CircularProgressIndicator()),
                );
              } else if (snapshot.hasError) {
                return Padding(
                  padding: const EdgeInsets.all(20.0),
                  child: Text(
                    'Error loading applicants: ${snapshot.error}',
                  ),
                );
              } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                return const Padding(
                  padding: EdgeInsets.all(20.0),
                  child: Text('No applicants found for this project.'),
                );
              }

              return Container(
                constraints: BoxConstraints(maxHeight: 400),
                padding: EdgeInsets.all(16),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      'Applicants for ${project.title}',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 18,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    SizedBox(height: 16),
                    Flexible(
                      child: ListView.builder(
                        shrinkWrap: true,
                        itemCount: snapshot.data!.length,
                        itemBuilder: (context, index) {
                          final applicant = snapshot.data![index];
                          return Card(
                            margin: EdgeInsets.only(bottom: 8),
                            child: Padding(
                              padding: EdgeInsets.all(8),
                              child: Row(
                                children: [
                                  CircleAvatar(
                                    child: Text(
                                      applicant['name']?.substring(0, 1) ?? '?',
                                    ),
                                  ),
                                  SizedBox(width: 12),
                                  Expanded(
                                    child: Text(
                                      applicant['name'] ?? 'Unknown',
                                    ),
                                  ),
                                  ElevatedButton(
                                    onPressed: () =>
                                        acceptApplicant(context, applicant),
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: Colors.green,
                                    ),
                                    child: Text('Accept'),
                                  ),
                                  SizedBox(width: 8),
                                  ElevatedButton(
                                    onPressed: () {
                                      // Reject logic will go here
                                    },
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: Colors.red,
                                    ),
                                    child: Text('Reject'),
                                  ),
                                ],
                              ),
                            ),
                          );
                        },
                      ),
                    ),
                    SizedBox(height: 8),
                    TextButton(
                      onPressed: () => Navigator.of(context).pop(),
                      child: Text('Close'),
                    ),
                  ],
                ),
              );
            },
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () => showApplicantDialog(context),
      child: Card(
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Column(
            children: [
              Row(
                children: [
                  Text(project.title),
                  Spacer(),
                  Icon(Icons.person),
                  SizedBox(width: 8),
                  Text('2'), // TODO remove hardcode
                ],
              ),
              SizedBox(height: 8),
              Wrap(
                crossAxisAlignment: WrapCrossAlignment.center,
                spacing: 4,
                children: [
                  ...project.skills
                      .sublist(
                        0,
                        project.skills.length > 2 ? 2 : project.skills.length,
                      )
                      .map(
                        (v) => OutlinedButton(onPressed: () {}, child: Text(v)),
                      ),
                  if (project.skills.length > 2) Text('...'),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
