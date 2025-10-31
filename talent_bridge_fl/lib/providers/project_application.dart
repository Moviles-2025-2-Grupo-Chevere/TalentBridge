class ProjectApplication {
  final String userId;
  final String projectId;
  final String createdById;

  ProjectApplication({
    required this.userId,
    required this.projectId,
    required this.createdById,
  });

  // Convert to Map for storage
  Map<String, dynamic> toMap() {
    return {
      'userId': userId,
      'projectId': projectId,
      'createdById': createdById,
    };
  }

  // Create from Map
  factory ProjectApplication.fromMap(Map<String, dynamic> map) {
    return ProjectApplication(
      userId: map['userId'],
      projectId: map['projectId'],
      createdById: map['createdById'],
    );
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ProjectApplication &&
          userId == other.userId &&
          projectId == other.projectId &&
          createdById == other.createdById;

  @override
  int get hashCode =>
      userId.hashCode ^ projectId.hashCode ^ createdById.hashCode;
}
