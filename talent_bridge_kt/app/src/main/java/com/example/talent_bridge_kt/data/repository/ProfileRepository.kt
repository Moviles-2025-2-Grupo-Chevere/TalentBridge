package com.example.talent_bridge_kt.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.talent_bridge_kt.data.cache.ProfileMemoryCache
import com.example.talent_bridge_kt.data.cache.ProfileSummary

class ProfileRepository(
    private val firestore: FirebaseFirestore,
    private val collectionName: String = "users"
) {
    private val TTL_MS = 5 * 60 * 1000L

    private fun keyForUid(uid: String) = "uid:$uid"


    suspend fun getProfileByUid(uid: String): ProfileSummary? {
        ProfileMemoryCache.get(keyForUid(uid))?.let {
            println("🟢 CACHE HIT para uid=$uid → ${it.displayName}")
            return it
        }

        println("🔵 CACHE MISS → leyendo de Firestore ($uid)")
        val snap = firestore.collection(collectionName).document(uid).get().await()
        if (!snap.exists()) {
            println("⚠️ Usuario no encontrado en Firestore ($uid)")
            return null
        }

        val profile = ProfileSummary(
            displayName = snap.getString("displayName"),
            avatarUrl = snap.getString("avatarUrl")
        )

        println("🟣 Guardando en caché uid=$uid (${profile.displayName})")
        ProfileMemoryCache.put(keyForUid(uid), profile, TTL_MS)
        return profile
    }


    suspend fun refreshByUid(uid: String): ProfileSummary? {
        ProfileMemoryCache.clear(keyForUid(uid))
        return getProfileByUid(uid)
    }
}