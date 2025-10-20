import 'package:sqflite/sqflite.dart' as sql;
import 'package:sqflite/sqlite_api.dart';
import 'package:path/path.dart' as path;
import 'package:talent_bridge_fl/domain/project_entity.dart';

class DbService {
  const DbService();

  final projectTable = 'projects';

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
      await db.insert(projectTable, p.toLocalDbMap(true));
    } catch (e) {
      throw Error();
    }
  }

  Future<void> removeSavedProject(String pId) async {
    final db = await _getDB();
    print("Deleting project with id $pId");
    try {
      final result = await db.delete(
        projectTable,
        where: "id = ?",
        whereArgs: [pId],
      );
      if (result != 1) {
        print("Project was not deleted");
        throw Error();
      }
    } catch (e) {
      throw Error();
    }
  }

  Future<List<ProjectEntity>> getSavedProjects() async {
    final db = await _getDB();
    try {
      return (await db.query(projectTable, where: "is_favorite = 1"))
          .map(
            (e) => ProjectEntity.fromLocalDbMap(e),
          )
          .toList();
    } catch (e) {
      throw Error();
    }
  }
}
