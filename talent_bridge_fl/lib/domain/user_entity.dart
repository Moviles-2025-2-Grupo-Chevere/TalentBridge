class UserEntity {
  UserEntity({
    required this.name,
    this.profilePictureUrl,
    required this.mainProgram,
    required this.description,
    required this.skills,
  });

  final String name;
  final String? profilePictureUrl;
  final String mainProgram;
  final String description;
  final List<String> skills;
}
