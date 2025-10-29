import 'package:talent_bridge_fl/domain/skill_entity.dart';

class SkillsService {
  static List<SkillEntity> getFallbackSkills() {
    var skills = [
      'JavaScript',
      'Dart',
      'Flutter',
      'React',
      'Node.js',
      'Firebase',
      'SQL',
      'RESTful APIs',
      'Git',
      'Agile Methodologies',
      'UI/UX Design',
      'Test-Driven Development',
      'Continuous Integration',
      'Cloud Computing',
      'Android Development',
      'iOS Development',
      'Performance Optimization',
      'Data Structures & Algorithms',
      'Problem Solving',
      'Leadership',
    ];
    return skills.map((e) => SkillEntity(e, null)).toList();
  }
}
