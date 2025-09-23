import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';

class ProjectService {
  getProjects() {
    var assetsimages = 'assets/images';
    var dummyImgRoute = '$assetsimages/dummy_post_img.jpeg';
    UserEntity user1 = UserEntity(
      name: 'Juanito',
      profilePictureUrl: '$assetsimages/gumball.jpg',
    );
    UserEntity user2 = UserEntity(
      name: 'Maria',
      profilePictureUrl: '$assetsimages/bb_princess.jpeg',
    );
    UserEntity user3 = UserEntity(
      name: 'Carlos',
      profilePictureUrl: '$assetsimages/rigby.jpg',
    );
    UserEntity user4 = UserEntity(
      name: 'Ana',
      profilePictureUrl: '$assetsimages/anais.jpg',
    );
    UserEntity user5 = UserEntity(
      name: 'Pedro',
      profilePictureUrl: '$assetsimages/mordecai.jpeg',
    );
    UserEntity user6 = UserEntity(
      name: 'Sofia',
      profilePictureUrl: '$assetsimages/margaret.jpg',
    );
    //talent_bridge_fl/assets/images/dummy_post_img.jpeg

    var projects = [
      ProjectEntity(
        createdAt: DateTime.now(),
        createdBy: user1,
        title: 'Looking for designers!',
        description:
            'People interested in graphic design. We hope for an availability of 2 weekly hours.',
        skills: ['Design', 'Drawing', '2 hours'],
        imgUrl: dummyImgRoute,
      ),
      ProjectEntity(
        createdAt: DateTime.now().subtract(Duration(days: 1)),
        createdBy: user2,
        title: 'Mobile App Development Team',
        description:
            'Seeking Flutter developers for an innovative mobile application. Looking for passionate developers with experience in state management and API integration.',
        skills: ['Flutter', 'Dart', 'API Integration', 'State Management'],
      ),
      ProjectEntity(
        createdAt: DateTime.now().subtract(Duration(days: 2)),
        createdBy: user3,
        title: 'Web Development for Startup',
        description:
            'Building a modern web platform for a tech startup. Need full-stack developers with React and Node.js experience. Remote work available.',
        skills: ['React', 'Node.js', 'JavaScript', 'MongoDB', 'Remote'],
      ),
      ProjectEntity(
        createdAt: DateTime.now().subtract(Duration(days: 3)),
        createdBy: user4,
        title: 'Data Science Research Project',
        description:
            'Looking for data scientists and machine learning engineers for an academic research project on predictive analytics. Great opportunity for students.',
        skills: ['Python', 'Machine Learning', 'Data Analysis', 'Research'],
      ),
      ProjectEntity(
        createdAt: DateTime.now().subtract(Duration(days: 4)),
        createdBy: user5,
        title: 'Game Development Studio',
        description:
            'Indie game studio looking for Unity developers and 3D artists. Working on an exciting adventure game with unique mechanics. Creative freedom guaranteed!',
        skills: ['Unity', 'C#', '3D Modeling', 'Game Design', 'Creative'],
      ),
      ProjectEntity(
        createdAt: DateTime.now().subtract(Duration(days: 5)),
        createdBy: user6,
        title: 'AI Chatbot Development',
        description:
            'Developing an intelligent chatbot for customer service automation. Looking for developers with NLP experience and backend development skills.',
        skills: ['AI', 'NLP', 'Python', 'Backend', 'Customer Service'],
      ),
    ];

    return projects;
  }
}
