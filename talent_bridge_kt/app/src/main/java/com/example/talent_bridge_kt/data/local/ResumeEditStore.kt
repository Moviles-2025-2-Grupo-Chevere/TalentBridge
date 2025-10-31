package com.example.talent_bridge_kt.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private val Context.resumeEditDataStore by preferencesDataStore(name = "resume_edit_prefs")


data class PendingResumeEdit(
    val resumeId: String,
    val fileName: String? = null,  // null means no change
    val language: String? = null    // null means no change
)

class ResumeEditStore(private val context: Context) {

    companion object {
        private val KEY_LOCAL_RESUMES = stringPreferencesKey("local_resumes_json")
        private val KEY_PENDING_EDITS_QUEUE = stringPreferencesKey("pending_edits_queue")
        private val KEY_PENDING_DELETES_QUEUE = stringPreferencesKey("pending_deletes_queue")
    }


    suspend fun saveLocalResumes(resumesJson: String) = withContext(Dispatchers.IO) {
        context.resumeEditDataStore.edit { prefs ->
            prefs[KEY_LOCAL_RESUMES] = resumesJson
        }
    }


    suspend fun getLocalResumes(): String? = withContext(Dispatchers.IO) {
        context.resumeEditDataStore.data.first()[KEY_LOCAL_RESUMES]
    }


    suspend fun addPendingEdit(edit: PendingResumeEdit) = withContext(Dispatchers.IO) {
        context.resumeEditDataStore.edit { prefs ->
            val currentQueue = prefs[KEY_PENDING_EDITS_QUEUE] ?: "[]"
            val queueArray = JSONArray(currentQueue)
            
            // Add new edit to queue
            val editObj = JSONObject().apply {
                put("resumeId", edit.resumeId)
                edit.fileName?.let { put("fileName", it) }
                edit.language?.let { put("language", it) }
                put("createdAt", System.currentTimeMillis())
            }
            queueArray.put(editObj)
            
            prefs[KEY_PENDING_EDITS_QUEUE] = queueArray.toString()
        }
    }


    suspend fun getPendingEdits(): List<PendingResumeEdit> = withContext(Dispatchers.IO) {
        val queueJson = context.resumeEditDataStore.data.first()[KEY_PENDING_EDITS_QUEUE] ?: return@withContext emptyList()
        
        try {
            val queueArray = JSONArray(queueJson)
            (0 until queueArray.length()).mapNotNull { i ->
                val obj = queueArray.getJSONObject(i)
                PendingResumeEdit(
                    resumeId = obj.getString("resumeId"),
                    fileName = obj.optString("fileName").takeIf { obj.has("fileName") },
                    language = obj.optString("language").takeIf { obj.has("language") }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun clearPendingEdits() = withContext(Dispatchers.IO) {
        context.resumeEditDataStore.edit { prefs ->
            prefs.remove(KEY_PENDING_EDITS_QUEUE)
        }
    }


    suspend fun applyLocalEdit(resumeId: String, fileName: String?, language: String?) = withContext(Dispatchers.IO) {
        val localResumesJson = getLocalResumes() ?: return@withContext
        
        try {
            val resumesArray = JSONArray(localResumesJson)
            val updatedArray = JSONArray()
            
            for (i in 0 until resumesArray.length()) {
                val resumeObj = resumesArray.getJSONObject(i)
                if (resumeObj.getString("id") == resumeId) {
                    // Apply edit
                    val updatedObj = JSONObject(resumeObj.toString())
                    fileName?.let { updatedObj.put("fileName", it) }
                    language?.let { updatedObj.put("language", it) }
                    updatedArray.put(updatedObj)
                } else {
                    updatedArray.put(resumeObj)
                }
            }
            
            saveLocalResumes(updatedArray.toString())
        } catch (e: Exception) {
            // Error applying edit
        }
    }


    suspend fun addPendingDelete(resumeId: String, storagePath: String?) = withContext(Dispatchers.IO) {
        context.resumeEditDataStore.edit { prefs ->
            val currentQueue = prefs[KEY_PENDING_DELETES_QUEUE] ?: "[]"
            val queueArray = JSONArray(currentQueue)
            
            // Add new delete to queue
            val deleteObj = JSONObject().apply {
                put("resumeId", resumeId)
                storagePath?.let { put("storagePath", it) }
                put("createdAt", System.currentTimeMillis())
            }
            queueArray.put(deleteObj)
            
            prefs[KEY_PENDING_DELETES_QUEUE] = queueArray.toString()
        }
    }


    suspend fun getPendingDeletes(): List<Pair<String, String?>> = withContext(Dispatchers.IO) {
        val queueJson = context.resumeEditDataStore.data.first()[KEY_PENDING_DELETES_QUEUE] ?: return@withContext emptyList()
        
        try {
            val queueArray = JSONArray(queueJson)
            (0 until queueArray.length()).mapNotNull { i ->
                val obj = queueArray.getJSONObject(i)
                val resumeId = obj.getString("resumeId")
                val storagePath = obj.optString("storagePath").takeIf { obj.has("storagePath") }
                resumeId to storagePath
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun clearPendingDeletes() = withContext(Dispatchers.IO) {
        context.resumeEditDataStore.edit { prefs ->
            prefs.remove(KEY_PENDING_DELETES_QUEUE)
        }
    }


    suspend fun removePendingDeletes(resumeIds: Set<String>) = withContext(Dispatchers.IO) {
        val currentQueue = context.resumeEditDataStore.data.first()[KEY_PENDING_DELETES_QUEUE] ?: return@withContext
        if (resumeIds.isEmpty()) return@withContext
        
        try {
            val queueArray = JSONArray(currentQueue)
            val updatedArray = JSONArray()
            
            for (i in 0 until queueArray.length()) {
                val obj = queueArray.getJSONObject(i)
                val resumeId = obj.getString("resumeId")
                if (resumeId !in resumeIds) {
                    updatedArray.put(obj)
                }
            }
            
            context.resumeEditDataStore.edit { prefs ->
                if (updatedArray.length() > 0) {
                    prefs[KEY_PENDING_DELETES_QUEUE] = updatedArray.toString()
                } else {
                    prefs.remove(KEY_PENDING_DELETES_QUEUE)
                }
            }
        } catch (e: Exception) {
            // Error processing queue
        }
    }


    suspend fun removeLocalResume(resumeId: String) = withContext(Dispatchers.IO) {
        val localResumesJson = getLocalResumes() ?: return@withContext
        
        try {
            val resumesArray = JSONArray(localResumesJson)
            val updatedArray = JSONArray()
            
            for (i in 0 until resumesArray.length()) {
                val resumeObj = resumesArray.getJSONObject(i)
                if (resumeObj.getString("id") != resumeId) {
                    updatedArray.put(resumeObj)
                }
            }
            
            saveLocalResumes(updatedArray.toString())
        } catch (e: Exception) {
            // Error removing resume
        }
    }
}

