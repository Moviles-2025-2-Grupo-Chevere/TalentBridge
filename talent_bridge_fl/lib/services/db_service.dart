import 'package:sqflite/sqflite.dart' as sql;
import 'package:sqflite/sqlite_api.dart';
import 'package:path/path.dart' as path;
import 'package:talent_bridge_fl/domain/project_entity.dart';

class DbService {
  const DbService();

  Future<void> onCreateDB(Database db, int version) {
    return db.execute(
      '''
        CREATE TABLE projects (
        id TEXT PRIMARY KEY,
        created_at TEXT,              -- ISO-8601 string (e.g., '2025-10-19T23:55:00Z')
        created_by_id TEXT NOT NULL,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        skills TEXT NOT NULL,         -- JSON-encoded array of strings
        img_url TEXT,                 -- Nullable image URL
        is_favorite INTEGER DEFAULT 0 -- 0 = false, 1 = true (optional local flag)
      ); ''',
    );
  }

  Future<Database> _getDB() async {
    final dbPath = await sql.getDatabasesPath();
    final db = await sql.openDatabase(
      path.join(dbPath, 'talent_bridge.db'),
      onCreate: onCreateDB,
      version: 1,
    );
    return db;
  }

  Future<void> insertSavedProject(ProjectEntity p) async {
    final db = await _getDB();
    try {
      await db.insert('projects', p.toLocalDbMap(true));
    } catch (e) {
      throw Error();
    }
  }
}
