import 'dart:convert';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:uuid/uuid.dart';

final _uuid = Uuid();

class ProjectEntity {
  final String? id;
  final DateTime? createdAt;
  final String createdById;
  final UserEntity? createdBy;
  final String title;
  final String description;
  final List<String> skills;
  final String? imgUrl;

  ProjectEntity({
    String? id,
    this.createdAt,
    required this.createdById,
    this.createdBy,
    required this.title,
    required this.description,
    required this.skills,
    this.imgUrl,
  }) : id = id ?? _uuid.v4().toString();

  factory ProjectEntity.fromMap(Map<String, dynamic> map) {
    DateTime? createdAtDate;

    try {
      if (map['createdAt'] != null) {
        createdAtDate = (map['createdAt'] as Timestamp).toDate();
      }
    } catch (e) {
      print('Error parsing createdAt: $e');
    }
    return ProjectEntity(
      id: map['id'] ?? '',
      createdAt: createdAtDate,
      createdById: map['createdById'] ?? '',
      title: map['title'] ?? '',
      description: map['description'] ?? '',
      skills: List<String>.from(map['skills'] ?? []),
      imgUrl: map['imgUrl'] ?? 'assets/images/dummy_post_img.jpeg',
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'createdAt': createdAt != null ? Timestamp.fromDate(createdAt!) : null,
      'createdById': createdById,
      'title': title,
      'description': description,
      'skills': List<String>.from(skills),
      'imgUrl': imgUrl,
    };
  }

  Map<String, dynamic> toLocalDbMap(bool isFavorite) {
    return {
      'id': id,
      'created_at': createdAt?.toIso8601String(),
      'created_by_id': createdById,
      'title': title,
      'description': description,
      'skills': jsonEncode(skills),
      'img_url': imgUrl,
      'is_favorite': isFavorite ? 1 : 0,
    };
  }
}
