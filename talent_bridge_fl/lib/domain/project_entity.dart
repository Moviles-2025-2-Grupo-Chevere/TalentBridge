import 'package:talent_bridge_fl/domain/user_entity.dart';

class ProjectEntity {
  ProjectEntity({
    required this.createdAt,
    required this.createdBy,
    required this.title,
    required this.description,
    required this.skills,
  });

  final DateTime createdAt;
  final UserEntity createdBy;
  final String title;
  final String description;
  final List<String> skills;
}
