package com.example.talent_bridge_kt.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.talent_bridge_kt.domain.model.Profile
import com.example.talent_bridge_kt.domain.model.Project
import com.google.firebase.Timestamp
import org.json.JSONArray
import org.json.JSONObject

class ProfileDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "profile_db"
        private const val DATABASE_VERSION = 1

        // Profile table
        private const val TABLE_PROFILE = "profile"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_EMAIL = "email"
        private const val COL_HEADLINE = "headline"
        private const val COL_IS_PUBLIC = "is_public"
        private const val COL_LINKEDIN = "linkedin"
        private const val COL_LOCATION = "location"
        private const val COL_PHONE = "phone"
        private const val COL_AVATAR_URL = "avatar_url"
        private const val COL_TAGS = "tags"
        private const val COL_BIO = "bio"
        private const val COL_PROJECTS = "projects"
        private const val COL_PROJECTS_UPDATED_AT = "projects_updated_at"
        private const val COL_LAST_SYNCED = "last_synced"
        private const val COL_IS_DIRTY = "is_dirty"

        // Pending updates table
        private const val TABLE_PENDING_UPDATES = "pending_updates"
        private const val COL_UPDATE_ID = "update_id"
        private const val COL_PROFILE_DATA = "profile_data"
        private const val COL_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Profile table
        db.execSQL("""
            CREATE TABLE $TABLE_PROFILE (
                $COL_ID TEXT PRIMARY KEY,
                $COL_NAME TEXT NOT NULL,
                $COL_EMAIL TEXT NOT NULL,
                $COL_HEADLINE TEXT,
                $COL_IS_PUBLIC INTEGER DEFAULT 1,
                $COL_LINKEDIN TEXT,
                $COL_LOCATION TEXT,
                $COL_PHONE TEXT,
                $COL_AVATAR_URL TEXT,
                $COL_TAGS TEXT,
                $COL_BIO TEXT,
                $COL_PROJECTS TEXT,
                $COL_PROJECTS_UPDATED_AT INTEGER,
                $COL_LAST_SYNCED INTEGER,
                $COL_IS_DIRTY INTEGER DEFAULT 0
            )
        """.trimIndent())

        // Pending updates table
        db.execSQL("""
            CREATE TABLE $TABLE_PENDING_UPDATES (
                $COL_UPDATE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PROFILE_DATA TEXT NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PENDING_UPDATES")
        onCreate(db)
    }

    fun saveProfile(profile: Profile, isDirty: Boolean = false) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, profile.id)
            put(COL_NAME, profile.name)
            put(COL_EMAIL, profile.email)
            put(COL_HEADLINE, profile.headline)
            put(COL_IS_PUBLIC, if (profile.isPublic) 1 else 0)
            put(COL_LINKEDIN, profile.linkedin)
            put(COL_LOCATION, profile.location)
            put(COL_PHONE, profile.phone)
            put(COL_AVATAR_URL, profile.avatarUrl)
            put(COL_TAGS, JSONArray(profile.tags).toString())
            put(COL_BIO, profile.bio)
            put(COL_PROJECTS, serializeProjects(profile.projects))
            put(COL_PROJECTS_UPDATED_AT, profile.projectsUpdatedAt)
            put(COL_LAST_SYNCED, System.currentTimeMillis())
            put(COL_IS_DIRTY, if (isDirty) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getProfile(userId: String): Profile? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROFILE,
            null,
            "$COL_ID = ?",
            arrayOf(userId),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val profile = cursorToProfile(cursor)
            cursor.close()
            db.close()
            profile
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    fun markProfileDirty(userId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_DIRTY, 1)
        }
        db.update(TABLE_PROFILE, values, "$COL_ID = ?", arrayOf(userId))
        db.close()
    }

    fun clearDirtyFlag(userId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_DIRTY, 0)
            put(COL_LAST_SYNCED, System.currentTimeMillis())
        }
        db.update(TABLE_PROFILE, values, "$COL_ID = ?", arrayOf(userId))
        db.close()
    }

    fun isProfileDirty(userId: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROFILE,
            arrayOf(COL_IS_DIRTY),
            "$COL_ID = ?",
            arrayOf(userId),
            null,
            null,
            null
        )
        val isDirty = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_DIRTY)) == 1
        } else {
            false
        }
        cursor.close()
        db.close()
        return isDirty
    }

    fun addPendingUpdate(profile: Profile) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PROFILE_DATA, serializeProfileForPending(profile))
            put(COL_CREATED_AT, System.currentTimeMillis())
        }
        db.insert(TABLE_PENDING_UPDATES, null, values)
        db.close()
    }

    fun getPendingUpdates(): List<Profile> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PENDING_UPDATES,
            null,
            null,
            null,
            null,
            null,
            "$COL_CREATED_AT ASC"
        )
        val updates = mutableListOf<Profile>()
        while (cursor.moveToNext()) {
            val profileJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROFILE_DATA))
            deserializeProfileFromPending(profileJson)?.let { updates.add(it) }
        }
        cursor.close()
        db.close()
        return updates
    }

    fun clearPendingUpdates() {
        val db = writableDatabase
        db.delete(TABLE_PENDING_UPDATES, null, null)
        db.close()
    }

    private fun cursorToProfile(cursor: android.database.Cursor): Profile {
        val tagsJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_TAGS))
        val tags = try {
            JSONArray(tagsJson).let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            }
        } catch (e: Exception) {
            emptyList()
        }

        val projectsJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROJECTS))
        val projects = try {
            deserializeProjects(projectsJson)
        } catch (e: Exception) {
            emptyList()
        }

        return Profile(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
            headline = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(COL_HEADLINE)),
            isPublic = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_PUBLIC)) == 1,
            linkedin = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(COL_LINKEDIN)),
            location = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(COL_LOCATION)),
            phone = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(COL_PHONE)),
            avatarUrl = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(COL_AVATAR_URL)),
            tags = tags,
            bio = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(COL_BIO)),
            projects = projects,
            projectsUpdatedAt = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(COL_PROJECTS_UPDATED_AT))
        )
    }

    private fun android.database.Cursor.getStringOrNull(columnIndex: Int): String? {
        return if (isNull(columnIndex)) null else getString(columnIndex)
    }

    private fun android.database.Cursor.getLongOrNull(columnIndex: Int): Long? {
        return if (isNull(columnIndex)) null else getLong(columnIndex)
    }

    private fun serializeProjects(projects: List<Project>): String {
        return JSONArray().apply {
            projects.forEach { project ->
                put(JSONObject().apply {
                    put("id", project.id)
                    put("title", project.title)
                    put("subtitle", project.subtitle ?: JSONObject.NULL)
                    put("description", project.description)
                    put("skills", JSONArray(project.skills))
                    put("imgUrl", project.imgUrl ?: JSONObject.NULL)
                    put("createdAt", project.createdAt?.toDate()?.time ?: JSONObject.NULL)
                    put("createdById", project.createdById)
                })
            }
        }.toString()
    }

    private fun deserializeProjects(json: String): List<Project> {
        return try {
            JSONArray(json).let { arr ->
                (0 until arr.length()).mapNotNull { i ->
                    val obj = arr.getJSONObject(i)
                    val createdAtValue = obj.opt("createdAt")
                    val createdAtMillis = when (createdAtValue) {
                        is Long -> createdAtValue
                        is Number -> createdAtValue.toLong()
                        else -> -1L
                    }
                    val skillsJson = obj.optString("skills", "[]")
                    Project(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        subtitle = obj.optString("subtitle", null).takeIf { !it.isNullOrEmpty() },
                        description = obj.getString("description"),
                        skills = try {
                            JSONArray(skillsJson).let { skillsArr ->
                                (0 until skillsArr.length()).map { skillsArr.getString(it) }
                            }
                        } catch (e: Exception) {
                            emptyList()
                        },
                        imgUrl = obj.optString("imgUrl", null).takeIf { !it.isNullOrEmpty() },
                        createdAt = if (createdAtMillis > 0) Timestamp(java.util.Date(createdAtMillis)) else null,
                        createdById = obj.getString("createdById")
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeProfileForPending(profile: Profile): String {
        return JSONObject().apply {
            put("id", profile.id)
            put("name", profile.name)
            put("email", profile.email)
            put("headline", profile.headline ?: JSONObject.NULL)
            put("isPublic", profile.isPublic)
            put("linkedin", profile.linkedin ?: JSONObject.NULL)
            put("location", profile.location ?: JSONObject.NULL)
            put("phone", profile.phone ?: JSONObject.NULL)
            put("avatarUrl", profile.avatarUrl ?: JSONObject.NULL)
            put("tags", JSONArray(profile.tags))
            put("bio", profile.bio ?: JSONObject.NULL)
            put("projects", serializeProjects(profile.projects))
            put("projectsUpdatedAt", profile.projectsUpdatedAt ?: JSONObject.NULL)
        }.toString()
    }

    private fun deserializeProfileFromPending(json: String): Profile? {
        return try {
            val obj = JSONObject(json)
            val tags = obj.getJSONArray("tags").let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            }
            val projects = deserializeProjects(obj.getString("projects"))
            val projectsUpdatedAtValue = obj.opt("projectsUpdatedAt")
            val projectsUpdatedAt = when (projectsUpdatedAtValue) {
                is Long -> if (projectsUpdatedAtValue > 0) projectsUpdatedAtValue else null
                is Number -> if (projectsUpdatedAtValue.toLong() > 0) projectsUpdatedAtValue.toLong() else null
                else -> null
            }
            Profile(
                id = obj.getString("id"),
                name = obj.getString("name"),
                email = obj.getString("email"),
                headline = obj.optString("headline", null).takeIf { !it.isNullOrEmpty() },
                isPublic = obj.optBoolean("isPublic", true),
                linkedin = obj.optString("linkedin", null).takeIf { !it.isNullOrEmpty() },
                location = obj.optString("location", null).takeIf { !it.isNullOrEmpty() },
                phone = obj.optString("phone", null).takeIf { !it.isNullOrEmpty() },
                avatarUrl = obj.optString("avatarUrl", null).takeIf { !it.isNullOrEmpty() },
                tags = tags,
                bio = obj.optString("bio", null).takeIf { !it.isNullOrEmpty() },
                projects = projects,
                projectsUpdatedAt = projectsUpdatedAt
            )
        } catch (e: Exception) {
            null
        }
    }
}

