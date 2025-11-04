package com.example.talent_bridge_kt.data.local

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

private val Context.avatarDataStore by preferencesDataStore(name = "avatar_prefs")

class AvatarStorageManager(private val context: Context) {

    companion object {
        private const val AVATAR_DIR_NAME = "avatars"
        private const val AVATAR_FILE_NAME = "avatar_local.jpg"
        
        private val KEY_LOCAL_AVATAR_PATH = stringPreferencesKey("local_avatar_path")
        private val KEY_PENDING_AVATAR_PATH = stringPreferencesKey("pending_avatar_path")
        private val KEY_HAS_PENDING_UPLOAD = booleanPreferencesKey("has_pending_upload")
    }


    suspend fun saveAvatarLocally(uri: Uri): Uri = withContext(Dispatchers.IO) {
        val avatarDir = File(context.filesDir, AVATAR_DIR_NAME).apply { mkdirs() }
        val avatarFile = File(avatarDir, AVATAR_FILE_NAME)
        

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(avatarFile).use { output ->
                input.copyTo(output)
            }
        }

        context.avatarDataStore.edit { prefs ->
            prefs[KEY_LOCAL_AVATAR_PATH] = avatarFile.absolutePath
        }
        

        context.avatarDataStore.edit { prefs ->
            prefs[KEY_PENDING_AVATAR_PATH] = avatarFile.absolutePath
            prefs[KEY_HAS_PENDING_UPLOAD] = true
        }
        
        android.net.Uri.fromFile(avatarFile)
    }


    suspend fun getLocalAvatarUri(): Uri? = withContext(Dispatchers.IO) {
        val path = context.avatarDataStore.data.first()[KEY_LOCAL_AVATAR_PATH] ?: return@withContext null
        val file = File(path)
        if (file.exists()) {
            android.net.Uri.fromFile(file)
        } else {
            null
        }
    }


    suspend fun getPendingAvatarPath(): String? = withContext(Dispatchers.IO) {
        context.avatarDataStore.data.first()[KEY_PENDING_AVATAR_PATH]
    }


    suspend fun hasPendingUpload(): Boolean = withContext(Dispatchers.IO) {
        context.avatarDataStore.data.first()[KEY_HAS_PENDING_UPLOAD] ?: false
    }


    suspend fun clearPendingUpload() = withContext(Dispatchers.IO) {
        context.avatarDataStore.edit { prefs ->
            prefs.remove(KEY_PENDING_AVATAR_PATH)
            prefs[KEY_HAS_PENDING_UPLOAD] = false
        }
    }


    suspend fun updateAvatarUrl(remoteUrl: String) = withContext(Dispatchers.IO) {

        clearPendingUpload()
    }
}

