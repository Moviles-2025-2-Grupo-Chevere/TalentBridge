import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/project_list.dart';
import 'package:talent_bridge_fl/data/project_service.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';
import 'package:firebase_performance/firebase_performance.dart'; // <-- NUEVO

class MainFeed extends StatelessWidget {
  MainFeed({super.key});

  final projectService = ProjectService();
  final _firebaseService = FirebaseService();

  static Trace? _ttfcProjects;
  static bool _started = false;
  static bool _stopped = false;
  // ----------------------------------------

  @override
  Widget build(BuildContext context) {
    if (!_started) {
      _started = true;
      _ttfcProjects = FirebasePerformance.instance.newTrace('ttfc_projects')
        ..start();
    }

    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: const [
            Text(
              'Explore Projects',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 36,
                color: Color.fromARGB(255, 255, 195, 0),
              ),
            ),
          ],
        ),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 0),
            child: FutureBuilder<List<ProjectEntity>>(
              future: _firebaseService.getAllProjects(),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(child: CircularProgressIndicator());
                } else if (snapshot.hasError) {
                  return const Center(
                    child: Text('There was an error getting the projects.'),
                  );
                } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                  return const Center(child: Text('No projects available'));
                } else {
                  if (!_stopped) {
                    WidgetsBinding.instance.addPostFrameCallback((_) {
                      if (!_stopped) {
                        _stopped = true;
                        _ttfcProjects?.stop();
                      }
                    });
                  }
                  return ProjectList(projects: snapshot.data!);
                }
              },
            ),
          ),
        ),
      ],
    );
  }
}
