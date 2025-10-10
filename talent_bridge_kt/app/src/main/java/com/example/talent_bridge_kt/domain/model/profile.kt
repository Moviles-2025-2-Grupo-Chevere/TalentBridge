package com.example.talent_bridge_kt.domain.model

data class Profile(
    val id: String,
    val name: String,
    val email: String,
    val headline: String? = null,
    val isPublic: Boolean = true,
    val linkedin: String? = null,
    val location: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val tags: List<String> = emptyList(),
    val bio: String? = null,
    val projects: List<Project> = emptyList(),
    val projectsUpdatedAt: Long? = null

)
