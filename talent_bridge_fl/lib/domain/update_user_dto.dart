class UpdateUserDto {
  UpdateUserDto({
    required this.displayName,
    required this.headline,
    required this.linkedin,
    required this.mobileNumber,
    required this.skillsOrTopics,
    required this.description,
    required this.major,
  });

  final String displayName;
  final String headline;
  final String linkedin;
  final String mobileNumber;
  final List<String> skillsOrTopics;
  final String description;
  final String major;

  Map<String, dynamic> toMap() {
    return {
      'displayName': displayName,
      'headline': headline,
      'linkedin': linkedin,
      'mobileNumber': mobileNumber,
      'skillsOrTopics': skillsOrTopics,
      'description': description,
      'major': major,
    };
  }
}
