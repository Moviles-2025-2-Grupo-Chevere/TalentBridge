import 'dart:convert';

import 'package:talent_bridge_fl/domain/project_entity.dart';

enum Source { remote, local }

class UserEntity {
  String id;
  String displayName;
  String email;
  String? headline;
  bool isPublic;
  String? linkedin;
  String? location;
  String? mobileNumber;
  String? photoUrl;
  List<ProjectEntity>? projects;
  List<String>? applications;
  List<String>? acceptedProjects;
  List<String>? skillsOrTopics;
  String? description;
  String? major;
  String? lastPortfolioUpdateAt;
  Source? source;

  UserEntity({
    required this.id,
    required this.displayName,
    required this.email,
    this.headline,
    this.isPublic = true,
    this.linkedin,
    this.location,
    this.mobileNumber,
    this.photoUrl,
    this.projects,
    this.applications,
    this.acceptedProjects,
    this.skillsOrTopics,
    this.description,
    this.major,
    this.lastPortfolioUpdateAt,
    this.source,
  });

  factory UserEntity.fromMap(Map<String, dynamic> map) {
    final mapProjects = map['projects'] as List<dynamic>? ?? [];
    return UserEntity(
      id: map['id'] ?? '',
      displayName: map['displayName'] ?? '',
      email: map['email'] ?? '',
      headline: map['headline'] ?? '',
      isPublic: map['isPublic'] ?? true,
      linkedin: map['linkedin'] ?? '',
      location: map['location'] ?? '',
      mobileNumber: map['mobileNumber'] ?? '',
      photoUrl: map['photoUrl'] ?? 'assets/images/gumball.jpg',
      projects: List<ProjectEntity>.from(
        mapProjects.map((i) => ProjectEntity.fromMap(i)),
      ), //CHECK TO MAP
      applications: List<String>.from(map['applications'] ?? []),
      acceptedProjects: List<String>.from(map['acceptedProjects'] ?? []),
      skillsOrTopics: List<String>.from(map['skillsOrTopics'] ?? []),
      description: map['description'] ?? '',
      major: map['major'] ?? '',
    );
  }

  Map<String, Object> toLocalMap() {
    return {
      'id': id,
      'display_name': displayName,
      'email': email,
      'headline': headline ?? '',
      'linkedin': linkedin ?? '',
      'location': location ?? '',
      'mobile_number': mobileNumber ?? '',
      'description': description ?? '',
      'major': major ?? '',
      'skills': skillsOrTopics != null ? jsonEncode(skillsOrTopics) : '[]',
    };
  }

  /// Create a [UserEntity] from a locally-stored map.
  factory UserEntity.fromLocalMap(Map<String, dynamic> map) {
    // Parse skills which may be stored as a JSON string or as a List.
    final dynamic skillsRaw = map['skills'];
    List<String>? skillsList = List<String>.from(jsonDecode(skillsRaw));

    return UserEntity(
      id: map['id']?.toString() ?? '',
      displayName: map['display_name'],
      email: map['email'] ?? '',
      headline: (map['headline'] ?? '')?.toString(),
      isPublic: true,
      linkedin: (map['linkedin'] ?? '')?.toString(),
      location: (map['location'] ?? '')?.toString(),
      mobileNumber: (map['mobile_number'] ?? '')?.toString(),
      photoUrl: 'assets/images/gumball.jpg',
      projects: [],
      applications: [],
      acceptedProjects: [],
      skillsOrTopics: skillsList,
      description: (map['description'] ?? '')?.toString(),
      major: (map['major'] ?? '')?.toString(),
      lastPortfolioUpdateAt: '',
    );
  }

  // ...existing code...
  UserEntity copyWith({
    String? id,
    String? displayName,
    String? email,
    String? headline,
    bool? isPublic,
    String? linkedin,
    String? location,
    String? mobileNumber,
    String? photoUrl,
    List<ProjectEntity>? projects,
    List<String>? applications,
    List<String>? acceptedProjects,
    List<String>? skillsOrTopics,
    String? description,
    String? major,
    String? lastPortfolioUpdateAt,
    Source? source,
  }) {
    return UserEntity(
      id: id ?? this.id,
      displayName: displayName ?? this.displayName,
      email: email ?? this.email,
      headline: headline ?? this.headline,
      isPublic: isPublic ?? this.isPublic,
      linkedin: linkedin ?? this.linkedin,
      location: location ?? this.location,
      mobileNumber: mobileNumber ?? this.mobileNumber,
      photoUrl: photoUrl ?? this.photoUrl,
      projects:
          projects ??
          (this.projects != null
              ? List<ProjectEntity>.from(this.projects!)
              : null),
      applications:
          applications ??
          (this.applications != null
              ? List<String>.from(this.applications!)
              : null),
      acceptedProjects:
          acceptedProjects ??
          (this.acceptedProjects != null
              ? List<String>.from(this.acceptedProjects!)
              : null),
      skillsOrTopics:
          skillsOrTopics ??
          (this.skillsOrTopics != null
              ? List<String>.from(this.skillsOrTopics!)
              : null),
      description: description ?? this.description,
      major: major ?? this.major,
      lastPortfolioUpdateAt:
          lastPortfolioUpdateAt ?? this.lastPortfolioUpdateAt,
      source: source ?? this.source,
    );
  }

  // ...existing code...
}
