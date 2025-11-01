package com.example.talent_bridge_kt.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.talent_bridge_kt.domain.model.Profile
import com.example.talent_bridge_kt.domain.model.Project
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.profileDataStore by preferencesDataStore(name = "profile_prefs")

class ProfilePreferencesStore(private val context: Context) {

    companion object {
        private val KEY_PROFILE_JSON = stringPreferencesKey("profile_json")
        private val KEY_IS_DIRTY = booleanPreferencesKey("is_dirty")
        private val KEY_PENDING_PROFILE_JSON = stringPreferencesKey("pending_profile_json")
    }

    suspend fun saveProfile(profile: Profile, isDirty: Boolean = false) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_PROFILE_JSON] = serializeProfile(profile)
            prefs[KEY_IS_DIRTY] = isDirty
            // Clear pending if saving as clean
            if (!isDirty) {
                prefs.remove(KEY_PENDING_PROFILE_JSON)
            }
        }
    }

    suspend fun getProfile(): Profile? {
        val prefs = context.profileDataStore.data.first()
        val json = prefs[KEY_PROFILE_JSON] ?: return null
        return deserializeProfile(json)
    }

    suspend fun isDirty(): Boolean {
        return context.profileDataStore.data.first().let { prefs ->
            prefs[KEY_IS_DIRTY] ?: false
        }
    }

    suspend fun savePendingProfile(profile: Profile) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_PENDING_PROFILE_JSON] = serializeProfile(profile)
            prefs[KEY_IS_DIRTY] = true
        }
    }

    suspend fun getPendingProfile(): Profile? {
        return context.profileDataStore.data.first().let { prefs ->
            val json = prefs[KEY_PENDING_PROFILE_JSON] ?: return null
            deserializeProfile(json)
        }
    }

    suspend fun clearPendingProfile() {
        context.profileDataStore.edit { prefs ->
            prefs.remove(KEY_PENDING_PROFILE_JSON)
            prefs[KEY_IS_DIRTY] = false
        }
    }

    private fun serializeProfile(profile: Profile): String {
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

    private fun deserializeProfile(json: String): Profile? {
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
}

