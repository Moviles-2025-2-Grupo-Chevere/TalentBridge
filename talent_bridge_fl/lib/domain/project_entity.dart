import 'package:talent_bridge_fl/domain/user_entity.dart';

class ProjectEntity {
  ProjectEntity({
    required this.createdAt,
    required this.createdById,
    this.createdBy,
    required this.title,
    required this.description,
    required this.skills,
    this.imgUrl,
  });

  final DateTime createdAt;
  final String createdById;
  final UserEntity? createdBy;
  final String title;
  final String description;
  final List<String> skills;
  final String? imgUrl;
}
