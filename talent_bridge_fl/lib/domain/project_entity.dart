import 'dart:convert';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:uuid/uuid.dart';

final _uuid = Uuid();

class ProjectEntity {
  String? id;
  DateTime? createdAt;
  String createdById;
  UserEntity? createdBy;
  String title;
  String description;
  List<String> skills;
  String? imgUrl;
  bool isFavorite;

  ProjectEntity({
    String? id,
    this.createdAt,
    required this.createdById,
    this.createdBy,
    required this.title,
    required this.description,
    required this.skills,
    this.imgUrl,
    this.isFavorite = false,
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

  factory ProjectEntity.fromLocalDbMap(Map<String, dynamic> map) {
    DateTime? createdAtDate;
    try {
      final createdAtRaw = map['created_at'];
      if (createdAtRaw is String && createdAtRaw.isNotEmpty) {
        createdAtDate = DateTime.tryParse(createdAtRaw);
      }
    } catch (e) {
      print('Error parsing created_at: $e');
    }

    List<String> parsedSkills = [];
    try {
      final skillsRaw = map['skills'];
      if (skillsRaw is String && skillsRaw.isNotEmpty) {
        final decoded = jsonDecode(skillsRaw);
        if (decoded is List) {
          parsedSkills = decoded.map((e) => e.toString()).toList();
        }
      }
    } catch (e) {
      print('Error parsing skills from local DB map: $e');
    }

    return ProjectEntity(
      id: map['id'] ?? '',
      createdAt: createdAtDate,
      createdById: map['created_by_id'] ?? '',
      title: map['title'] ?? '',
      description: map['description'] ?? '',
      skills: parsedSkills,
      imgUrl: map['img_url'] ?? 'assets/images/dummy_post_img.jpeg',
      isFavorite: map['is_favorite'] == 1,
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

  /// Returns a short relative time string for `createdAt`: "5m", "2h", "3d", "1w", etc.
  /// Returns "-" when `createdAt` is null.
  String get timeAgo {
    if (createdAt == null) return '-';
    final diff = DateTime.now().difference(createdAt!);
    final minutes = diff.inMinutes;
    if (minutes < 1) return '0m';
    if (minutes < 60) return '${minutes}m';
    final hours = diff.inHours;
    if (hours < 24) return '${hours}h';
    final days = diff.inDays;
    if (days < 7) return '${days}d';
    final weeks = days ~/ 7;
    return '${weeks}w';
  }
}
