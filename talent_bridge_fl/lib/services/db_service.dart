import 'package:sqflite/sqflite.dart' as sql;
import 'package:sqflite/sqlite_api.dart';
import 'package:path/path.dart' as path;
import 'package:talent_bridge_fl/domain/project_entity.dart';
import 'package:talent_bridge_fl/domain/user_entity.dart';
import 'package:talent_bridge_fl/services/firebase_service.dart';

class DbService {
  DbService();

  final projectTable = 'projects';
  final usersTable = 'users';
  final fb = FirebaseService();

  Future<void> onCreateDB(Database db, int version) async {
    await db.execute(
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
      ); 
        
      ''',
    );

    await db.execute('''
      CREATE TABLE users (
              id TEXT PRIMARY KEY,
              display_name TEXT,
              email TEXT,
              headline TEXT,
              linkedin TEXT,
              location TEXT,
              mobile_number TEXT,
              description TEXT,
              major TEXT,
              skills TEXT -- JSON-encoded array of strings
              );
              ''');
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

  Future<void> saveProfileLocally(UserEntity u) async {
    final db = await _getDB();
    try {
      db.insert(usersTable, u.toLocalMap());
    } catch (e) {
      rethrow;
    }
  }

  Future<UserEntity?> getProfileLocally() async {
    final db = await _getDB();
    try {
      final uid = fb.currentUid();
      if (uid == null) throw Exception('Uid not found');
      var result = (await db.query(
        usersTable,
        where: "id = ?",
        limit: 1,
        whereArgs: [uid],
      ));
      if (result.length != 1) return null;
      return UserEntity.fromLocalMap(result[0]);
    } catch (e) {
      rethrow;
    }
  }
}
