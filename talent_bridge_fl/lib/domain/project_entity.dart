import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:uuid/uuid.dart';

final _uuid = Uuid();

class ProjectEntity {
  final String? id;
  final DateTime createdAt;
  final String createdById;
  final UserEntity? createdBy;
  final String title;
  final String description;
  final List<String> skills;
  final String? imgUrl;

  ProjectEntity({
    String? id,
    required this.createdAt,
    required this.createdById,
    this.createdBy,
    required this.title,
    required this.description,
    required this.skills,
    this.imgUrl,
  }) : id = id ?? _uuid.v4().toString();

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'createdAt': createdAt.toIso8601String(),
      'createdById': createdById,
      'title': title,
      'description': description,
      'skills': List<String>.from(skills),
      'imgUrl': imgUrl,
    };
  }
}
