class UserEntity {
  final String id;
  final String displayName;
  final String email;
  final String? headline;
  final bool isPublic;
  final String? linkedin;
  final String? location;
  final String? mobileNumber;
  final String? photoUrl;
  final List<String>? projects;
  final List<String>? skillsOrTopics;
  final String? description;
  final String? major;
  final String? lastPortfolioUpdateAt;

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
    this.skillsOrTopics,
    this.description,
    this.major,
    this.lastPortfolioUpdateAt,
  });

  factory UserEntity.fromMap(Map<String, dynamic> map) {
    return UserEntity(
      id: map['id'] ?? '',
      displayName: map['displayName'] ?? '',
      email: map['email'] ?? '',
      headline: map['headline'] ?? '',
      isPublic: map['isPublic'] ?? true,
      linkedin: map['linkedin'] ?? '',
      location: map['location'] ?? '',
      mobileNumber: map['mobileNumber'] ?? '',
      photoUrl: map['photoUrl'] ?? '',
      projects: List<String>.from(map['projects'] ?? []),
      skillsOrTopics: List<String>.from(map['skillsOrTopics'] ?? []),
      description: map['description'] ?? '',
      major: map['major'] ?? '',
      lastPortfolioUpdateAt: map['lastPortfolioUpdateAt'] ?? '',
    );
  }
}
