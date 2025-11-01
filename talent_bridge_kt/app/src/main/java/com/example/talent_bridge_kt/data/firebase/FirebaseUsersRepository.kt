package com.example.talent_bridge_kt.data.firebase

import com.example.talent_bridge_kt.domain.model.StudentListItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.talent_bridge_kt.data.firebase.model.FirestoreProject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseUsersRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: com.google.firebase.auth.FirebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
) {
    fun listenPublicStudents(onChange: (List<StudentListItem>) -> Unit): ListenerRegistration {
        return db.collection("users")
            .whereEqualTo("isPublic", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { d ->
                    val uid = d.id
                    val name = (d.getString("displayName") ?: "").ifBlank { "Student" }
                    val avatar = d.getString("avatarUrl")
                    val headline = d.getString("headline")
                    val bio = d.getString("bio")

                    @Suppress("UNCHECKED_CAST")
                    val skills = (d.get("skillsOrTopics") as? List<*>)?.filterIsInstance<String>()
                        ?: emptyList()
                    StudentListItem(
                        uid = uid,
                        displayName = name,
                        avatarUrl = avatar,
                        headline = headline,
                        bio = bio,
                        skillsOrTopics = skills
                    )
                } ?: emptyList()
                onChange(items)


                suspend fun addProjectToCurrentUser(project: FirestoreProject) {
                    val uid = auth.currentUser?.uid
                        ?: throw IllegalStateException("No hay usuario autenticado")

                    val userRef = db.collection("users").document(uid)
                    val update = mapOf(
                        "projects" to FieldValue.arrayUnion(project)
                    )
                    userRef.set(update, SetOptions.merge()).await()
                }
            }
    }
}

