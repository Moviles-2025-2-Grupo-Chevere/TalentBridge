package com.example.talent_bridge_kt.data.firebase

import com.example.talent_bridge_kt.domain.model.StudentListItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirebaseUsersRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
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
                    val skills = (d.get("skillsOrTopics") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
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
            }
    }
}

