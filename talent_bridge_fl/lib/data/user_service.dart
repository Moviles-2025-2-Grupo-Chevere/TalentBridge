import 'package:talent_bridge_fl/domain/user_entity.dart';

class UserService {
  getUsers() {
    var assetsimages = 'assets/images';

    var users = [
      UserEntity(
        id: '1',
        displayName: 'Juanito',
        email: 'juanito@example.com',
        headline: 'Creative Designer',
        isPublic: true,
        linkedin: '',
        location: 'Bogotá',
        mobileNumber: '',
        photoUrl: '$assetsimages/gumball.jpg',
        projects: [],
        skillsOrTopics: [
          'Adobe Photoshop',
          'Illustrator',
          'UI/UX Design',
          'Branding',
        ],
        description:
            'Creative designer with 3+ years of experience in visual communication and branding.',
        major: 'Graphic Design',
        lastPortfolioUpdateAt: '',
      ),
      UserEntity(
        id: '2',
        displayName: 'Maria',
        email: 'maria@example.com',
        headline: 'Full-stack Developer',
        isPublic: true,
        linkedin: '',
        location: 'Medellín',
        mobileNumber: '',
        photoUrl: '$assetsimages/bb_princess.jpeg',
        projects: [],
        skillsOrTopics: ['Flutter', 'React', 'Node.js', 'MongoDB', 'Git'],
        description:
            'Full-stack developer passionate about mobile app development and clean code.',
        major: 'Software Engineering',
        lastPortfolioUpdateAt: '',
      ),
      UserEntity(
        id: '3',
        displayName: 'Carlos',
        email: 'carlos@example.com',
        headline: 'Web Developer',
        isPublic: true,
        linkedin: '',
        location: 'Cali',
        mobileNumber: '',
        photoUrl: '$assetsimages/rigby.jpg',
        projects: [],
        skillsOrTopics: ['JavaScript', 'React', 'Vue.js', 'CSS', 'HTML'],
        description:
            'Web developer specializing in modern frontend frameworks and responsive design.',
        major: 'Computer Science',
        lastPortfolioUpdateAt: '',
      ),
      UserEntity(
        id: '4',
        displayName: 'Ana',
        email: 'ana@example.com',
        headline: 'Data Scientist',
        isPublic: true,
        linkedin: '',
        location: 'Barranquilla',
        mobileNumber: '',
        photoUrl: '$assetsimages/anais.jpg',
        projects: [],
        skillsOrTopics: ['Python', 'R', 'TensorFlow', 'Pandas', 'SQL'],
        description:
            'Data scientist with expertise in machine learning and statistical analysis.',
        major: 'Data Science',
        lastPortfolioUpdateAt: '',
      ),
      UserEntity(
        id: '5',
        displayName: 'Pedro',
        email: 'pedro@example.com',
        headline: 'Game Developer',
        isPublic: true,
        linkedin: '',
        location: 'Cartagena',
        mobileNumber: '',
        photoUrl: '$assetsimages/mordecai.jpeg',
        projects: [],
        skillsOrTopics: [
          'Unity',
          'C#',
          'Blender',
          '3D Animation',
          'Game Design',
        ],
        description:
            'Indie game developer with passion for creating immersive gaming experiences.',
        major: 'Game Development',
        lastPortfolioUpdateAt: '',
      ),
      UserEntity(
        id: '6',
        displayName: 'Sofia',
        email: 'sofia@example.com',
        headline: 'AI Engineer',
        isPublic: true,
        linkedin: '',
        location: 'Manizales',
        mobileNumber: '',
        photoUrl: '$assetsimages/margaret.jpg',
        projects: [],
        skillsOrTopics: [
          'Python',
          'NLP',
          'TensorFlow',
          'PyTorch',
          'Machine Learning',
        ],
        description:
            'AI engineer focused on natural language processing and chatbot development.',
        major: 'Artificial Intelligence',
        lastPortfolioUpdateAt: '',
      ),
    ];

    return users;
  }
}
