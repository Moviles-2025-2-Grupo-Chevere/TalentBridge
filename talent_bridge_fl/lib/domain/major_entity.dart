class MajorEntity {
  MajorEntity({required this.name, required this.code, this.icon});
  String name;
  String code;
  String? icon;

  factory MajorEntity.fromMap(Map<String, dynamic> map) {
    return MajorEntity(
      name: map['name'] as String,
      code: map['code'] as String,
      icon: map['icon'] as String?,
    );
  }
}
