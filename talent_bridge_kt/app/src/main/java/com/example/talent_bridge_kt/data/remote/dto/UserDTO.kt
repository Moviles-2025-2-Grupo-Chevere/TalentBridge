package com.example.talent_bridge_kt.data.remote.dto



import com.example.talent_bridge_kt.data.common.normalizeAsciiLower
import com.example.talent_bridge_kt.domain.model.User

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class UserDto(
    val id: String,
    val displayName: String?,
    val headline: String?,
    val photoUrl: String?,
    val location: String?,
    val skillsOrTopics: List<String>?,
    val isPublic: Boolean?,
    val email: String?,
    val linkedin: String?,
    val mobileNumber: String?,
    val projects: List<String>?,
    val description: String?,
) {
    fun toDomain(): User = User(
        id = id,
        displayName = displayName ?: "",
        headline = headline ?: "",
        photoUrl = photoUrl ?: "",
        location = location ?: "",
        skills = (skillsOrTopics ?: emptyList()).map { it.normalizeAsciiLower() },
        isPublic = isPublic == true,
        email = email ?: "",
        linkedin = linkedin ?: "",
        mobileNumber = mobileNumber ?: "",
        description = description ?: "",
        projects = (projects ?: emptyList())

    )

    companion object {
        fun from(doc: DocumentSnapshot): UserDto {
            val skills = (doc.get("skillsOrTopics") as? List<String>)
                ?: (doc.get("habilidades") as? List<String>)

            return UserDto(
                id = doc.id,
                displayName = doc.getString("displayName"),
                headline = doc.getString("headline"),
                photoUrl = doc.getString("photoUrl"),
                location = doc.getString("location"),
                skillsOrTopics = skills,
                isPublic = doc.getBoolean("isPublic"),
                email = doc.getString("email"),
                linkedin = doc.getString("linkedin"),
                mobileNumber = doc.getString("mobileNumber"),
                description = doc.getString("description"),
                projects = doc.get("projects") as? List<String>
            )
        }
    }

}
