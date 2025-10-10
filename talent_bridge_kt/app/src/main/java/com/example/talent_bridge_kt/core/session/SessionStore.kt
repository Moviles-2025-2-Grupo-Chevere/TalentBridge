package com.example.talent_bridge_kt.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_EMAIL   = stringPreferencesKey("email")
        private val KEY_TOKEN   = stringPreferencesKey("app_token") // si usas token del back
    }

    val userIdFlow: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val emailFlow:  Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] }
    val tokenFlow:  Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    suspend fun save(userId: String, email: String?, token: String?) {
        context.dataStore.edit {
            it[KEY_USER_ID] = userId
            email?.let  { e -> it[KEY_EMAIL] = e }
            token?.let  { t -> it[KEY_TOKEN] = t }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

