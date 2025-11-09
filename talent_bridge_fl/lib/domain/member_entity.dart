import 'dart:convert';

class MemberEntity {
  String id;
  String name;
  String description;

  MemberEntity({
    required this.id,
    required this.name,
    required this.description,
  });

  factory MemberEntity.fromMap(Map<String, dynamic> map) {
    return MemberEntity(
      id: map['id'] ?? '',
      name: map['Name'] ?? map['name'] ?? '',
      description: map['Description'] ?? map['description'] ?? '',
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'description': description,
    };
  }

  String toJson() => json.encode(toMap());

  factory MemberEntity.fromJson(String source) =>
      MemberEntity.fromMap(json.decode(source));
}
