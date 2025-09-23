import 'package:talent_bridge_fl/domain/user_entity.dart';

class UserService {
  getUsers() {
    var assetsimages = 'assets/images';

    var users = [
      UserEntity(
        name: 'Juanito',
        profilePictureUrl: '$assetsimages/gumball.jpg',
        mainProgram: 'Graphic Design',
        description:
            'Creative designer with 3+ years of experience in visual communication and branding.',
        skills: ['Adobe Photoshop', 'Illustrator', 'UI/UX Design', 'Branding'],
      ),
      UserEntity(
        name: 'Maria',
        profilePictureUrl: '$assetsimages/bb_princess.jpeg',
        mainProgram: 'Software Engineering',
        description:
            'Full-stack developer passionate about mobile app development and clean code.',
        skills: ['Flutter', 'React', 'Node.js', 'MongoDB', 'Git'],
      ),
      UserEntity(
        name: 'Carlos',
        profilePictureUrl: '$assetsimages/rigby.jpg',
        mainProgram: 'Computer Science',
        description:
            'Web developer specializing in modern frontend frameworks and responsive design.',
        skills: ['JavaScript', 'React', 'Vue.js', 'CSS', 'HTML'],
      ),
      UserEntity(
        name: 'Ana',
        profilePictureUrl: '$assetsimages/anais.jpg',
        mainProgram: 'Data Science',
        description:
            'Data scientist with expertise in machine learning and statistical analysis.',
        skills: ['Python', 'R', 'TensorFlow', 'Pandas', 'SQL'],
      ),
      UserEntity(
        name: 'Pedro',
        profilePictureUrl: '$assetsimages/mordecai.jpeg',
        mainProgram: 'Game Development',
        description:
            'Indie game developer with passion for creating immersive gaming experiences.',
        skills: ['Unity', 'C#', 'Blender', '3D Animation', 'Game Design'],
      ),
      UserEntity(
        name: 'Sofia',
        profilePictureUrl: '$assetsimages/margaret.jpg',
        mainProgram: 'Artificial Intelligence',
        description:
            'AI engineer focused on natural language processing and chatbot development.',
        skills: ['Python', 'NLP', 'TensorFlow', 'PyTorch', 'Machine Learning'],
      ),
    ];

    return users;
  }
}
