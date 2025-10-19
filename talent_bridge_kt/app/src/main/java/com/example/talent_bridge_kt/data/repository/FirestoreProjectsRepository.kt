// data/repository/FirestoreProjectsRepository.kt
package com.example.talent_bridge_kt.data.repository

import com.example.talent_bridge_kt.domain.model.Project
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreProjectsRepository {

    private val db = Firebase.firestore

    /** Lee todos los users y aplana el array "projects" de cada uno. */
    suspend fun fetchAllProjects(): List<Project> {
        val usersSnap = db.collection("users").get().await()
        val items = mutableListOf<Project>()

        for (doc in usersSnap.documents) {
            val uid = doc.id

            // Cast seguro
            val arr: List<Map<String, Any?>> =
                (doc.get("projects") as? List<*>)      // lista cruda
                    ?.mapNotNull { it as? Map<String, Any?> } // cada item a Map
                    ?: emptyList()

            for (p in arr) {
                items += Project(
                    id          = (p["id"] as? String).orElse(),
                    title       = (p["title"] as? String).orElse("Untitled"),
                    subtitle    = null,
                    description = (p["description"] as? String).orElse(),
                    skills      = (p["skills"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    imgUrl      = p["imgUrl"] as? String,
                    createdAt   = p.getTimestampFromProject(),
                    createdById = (p["createdById"] as? String) ?: uid
                )
            }
        }

        // Ordena por fecha desc (si existe) y luego por título
        return items.sortedWith(
            compareByDescending<Project> { it.createdAt?.toDate() }
                .thenBy { it.title.lowercase() }
        )
    }

    private fun Map<String, Any?>.getTimestampFromProject(): com.google.firebase.Timestamp? {
        return this["createdAt"] as? com.google.firebase.Timestamp
    }

    private fun String?.orElse(fallback: String = "") = this ?: fallback
}
