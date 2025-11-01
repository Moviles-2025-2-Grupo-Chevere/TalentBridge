import 'package:flutter/material.dart';
import 'package:talent_bridge_fl/components/project_list.dart';
import 'package:talent_bridge_fl/data/project_service.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

// BQ timer helper
import 'package:talent_bridge_fl/analytics/analytics_timer.dart';

class MainFeed extends StatefulWidget {
  const MainFeed({super.key});

  @override
  State<MainFeed> createState() => _MainFeedState();
}

class _MainFeedState extends State<MainFeed> {
  final projectService = ProjectService();
  final _firebaseService = FirebaseService();

  // ---- BQ: medir time-to-first-content de Projects list ----
  late final ScreenTimer _tProjects;
  bool _sent = false;

  @override
  void initState() {
    super.initState();
    _tProjects = ScreenTimer(
      'first_content_projects',
      baseParams: {
        'screen': 'Projects',
      },
    );
  }

  @override
  Widget build(BuildContext context) {
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
                // Loading
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(child: CircularProgressIndicator());
                }

                // Error
                if (snapshot.hasError) {
                  return const Center(
                    child: Text('There was an error getting the projects.'),
                  );
                }

                // Empty
                if (!snapshot.hasData || snapshot.data!.isEmpty) {
                  return const Center(child: Text('No projects available'));
                }

                // Primer contenido real (desde network)
                if (!_sent) {
                  _sent = true;
                  _tProjects.endOnce(
                    source: 'network',
                    itemCount: snapshot.data!.length,
                  );
                }

                return ProjectList(projects: snapshot.data!);
              },
            ),
          ),
        ),
      ],
    );
  }
}
