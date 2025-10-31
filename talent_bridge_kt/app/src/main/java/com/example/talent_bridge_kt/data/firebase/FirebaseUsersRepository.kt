package com.example.talent_bridge_kt.data.firebase

import com.example.talent_bridge_kt.data.cache.ProfileMemoryCache
import com.example.talent_bridge_kt.data.cache.ProfileSummary
import com.example.talent_bridge_kt.domain.model.StudentListItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirebaseUsersRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun listenPublicStudents(onChange: (List<StudentListItem>) -> Unit): ListenerRegistration {
        // 1. primero intento mostrar lo que ya había en RAM
        val cached = ProfileMemoryCache.snapshot()
            .mapNotNull { (key, prof) ->
                val uid = key.removePrefix("uid:")
                prof.toStudentListItem(uid)
            }

        if (cached.isNotEmpty()) {
            onChange(cached)
        }

        // 2. luego me suscribo a firestore
        return firestore.collection("users")
            .whereEqualTo("isPublic", true)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener

                val list = snap.documents.mapNotNull { d ->
                    val uid = d.id
                    val name = d.getString("displayName") ?: return@mapNotNull null
                    val avatar = d.getString("avatarUrl")
                    val headline = d.getString("headline")
                    val bio = d.getString("bio")
                    val skills = (d.get("skillsOrTopics") as? List<*>)?.map { it.toString() } ?: emptyList()
                    StudentListItem(uid, name, avatar, headline, bio, skills).also {
                        // guardo mínimo nombre+foto en RAM
                        ProfileMemoryCache.put("uid:$uid", ProfileSummary(name, avatar))
                    }
                }

                onChange(list)
            }
    }
}

private fun ProfileSummary.toStudentListItem(uid: String) =
    StudentListItem(
        uid = uid,
        displayName = this.displayName ?: "Unknown",
        avatarUrl = this.avatarUrl,
        headline = null,
        bio = null,
        skillsOrTopics = emptyList()
    )
