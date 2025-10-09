import 'package:talent_bridge_fl/data/user_service.dart';
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';

class ProjectService {
  getProjects() {
    var assetsimages = 'assets/images';
    var dummyImgRoute = '$assetsimages/dummy_post_img.jpeg';
    final userService = UserService();
    final users = userService.getUsers();
    //talent_bridge_fl/assets/images/dummy_post_img.jpeg

    var projects = [
      ProjectEntity(
        createdAt: DateTime.now(),
        createdById: users[0].id,
        createdBy: users[0],
        title: 'Looking for designers!',
        description:
            'People interested in graphic design. We hope for an availability of 2 weekly hours.',
        skills: ['Design', 'Drawing', '2 hours'],
        imgUrl: dummyImgRoute,
      ),
      ProjectEntity(
        createdById: users[0].id,
        createdAt: DateTime.now().subtract(Duration(days: 1)),
        createdBy: users[1],
        title: 'Mobile App Development Team',
        description:
            'Seeking Flutter developers for an innovative mobile application. Looking for passionate developers with experience in state management and API integration.',
        skills: ['Flutter', 'Dart', 'API Integration', 'State Management'],
      ),
      ProjectEntity(
        createdById: users[0].id,
        createdAt: DateTime.now().subtract(Duration(days: 2)),
        createdBy: users[2],
        title: 'Web Development for Startup',
        description:
            'Building a modern web platform for a tech startup. Need full-stack developers with React and Node.js experience. Remote work available.',
        skills: ['React', 'Node.js', 'JavaScript', 'MongoDB', 'Remote'],
      ),
      ProjectEntity(
        createdById: users[0].id,
        createdAt: DateTime.now().subtract(Duration(days: 3)),
        createdBy: users[3],
        title: 'Data Science Research Project',
        description:
            'Looking for data scientists and machine learning engineers for an academic research project on predictive analytics. Great opportunity for students.',
        skills: ['Python', 'Machine Learning', 'Data Analysis', 'Research'],
      ),
      ProjectEntity(
        createdById: users[0].id,
        createdAt: DateTime.now().subtract(Duration(days: 4)),
        createdBy: users[4],
        title: 'Game Development Studio',
        description:
            'Indie game studio looking for Unity developers and 3D artists. Working on an exciting adventure game with unique mechanics. Creative freedom guaranteed!',
        skills: ['Unity', 'C#', '3D Modeling', 'Game Design', 'Creative'],
      ),
      ProjectEntity(
        createdById: users[0].id,
        createdAt: DateTime.now().subtract(Duration(days: 5)),
        createdBy: users[5],
        title: 'AI Chatbot Development',
        description:
            'Developing an intelligent chatbot for customer service automation. Looking for developers with NLP experience and backend development skills.',
        skills: ['AI', 'NLP', 'Python', 'Backend', 'Customer Service'],
      ),
    ];

    return projects;
  }
}
