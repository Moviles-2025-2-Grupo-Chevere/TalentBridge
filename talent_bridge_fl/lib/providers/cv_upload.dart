class CVUpload {
  final String localPath;
  final String fileName;
  final DateTime queuedAt;

  CVUpload({
    required this.localPath,
    required this.fileName,
    required this.queuedAt,
  });

  Map<String, dynamic> toMap() {
    return {
      'localPath': localPath,
      'fileName': fileName,
      'queuedAt': queuedAt.toIso8601String(),
    };
  }

  factory CVUpload.fromMap(Map<String, dynamic> map) {
    return CVUpload(
      localPath: map['localPath'] as String,
      fileName: map['fileName'] as String,
      queuedAt: DateTime.parse(map['queuedAt'] as String),
    );
  }

  @override
  String toString() => 'CVUpload(fileName: $fileName, localPath: $localPath)';

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is CVUpload &&
        other.localPath == localPath &&
        other.fileName == fileName;
  }

  @override
  int get hashCode => localPath.hashCode ^ fileName.hashCode;
}
