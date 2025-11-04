// data/repository/FirestoreProjectsRepository.kt
package com.example.talent_bridge_kt.data.repository

import com.example.talent_bridge_kt.domain.model.Project
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreProjectsRepository {

    private val db = Firebase.firestore

    /** Lee todos los users y aplana el array "projects" de cada uno. */
    suspend fun fetchAllProjects(): List<Project> {
        val usersSnap = db.collection("users").get().await()
        return flattenUsersProjects(usersSnap.documents.map { it.data to it.id })
    }

    fun listenAllProjects(onChange: (List<Project>) -> Unit): ListenerRegistration {
        return db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val docs = snapshot?.documents ?: emptyList()
                val list = flattenUsersProjects(
                    docs.map { it.data to it.id }
                )

                onChange(list)
            }
    }

    private fun flattenUsersProjects(
        docs: List<Pair<Map<String, Any?>?, String>>
    ): List<Project> {
        val items = mutableListOf<Project>()
        for ((data, uid) in docs) {
            if (data == null) continue

            @Suppress("UNCHECKED_CAST")
            val arr: List<Map<String, Any?>> =
                (data["projects"] as? List<*>)      // lista cruda
                    ?.mapNotNull { it as? Map<String, Any?> }
                    ?: emptyList()

            for (p in arr) {
                items += Project(
                    id = (p["id"] as? String).orEmpty(),
                    title = (p["title"] as? String).orEmpty(),
                    subtitle = null,
                    description = (p["description"] as? String).orEmpty(),
                    skills = (p["skills"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    imgUrl = p["imgUrl"] as? String,
                    createdAt = p["createdAt"] as? com.google.firebase.Timestamp,
                    createdById = (p["createdById"] as? String) ?: uid
                )
            }
        }

        return items.sortedWith(
            compareByDescending<Project> { it.createdAt?.toDate() }
                .thenBy { it.title.lowercase() }
        )
    }
}
