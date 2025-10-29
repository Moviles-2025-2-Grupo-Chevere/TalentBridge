class SkillEntity {
  final String label;
  final String? icon;

  const SkillEntity(this.label, this.icon);

  factory SkillEntity.fromMap(Map<String, dynamic>? map) {
    final m = map ?? {};
    return SkillEntity(
      (m['label'] ?? '').toString(),
      m['icon']?.toString(),
    );
  }
}
