package com.example.talent_bridge_kt.domain.model

data class StudentListItem(
    val uid: String,
    val displayName: String,
    val avatarUrl: String?,
    val headline: String?,
    val bio: String?,
    val skillsOrTopics: List<String>
)


