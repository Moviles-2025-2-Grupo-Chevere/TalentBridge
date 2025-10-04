package com.example.talent_bridge_kt.domain.model

/**
 * Domain entity for a user/org profile.
 * This must be independent from any data source (Firebase, REST, Room, etc.).
 */
data class Profile(
    val id: String,
    val name: String,
    val email: String,
    val linkedin: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,   // Remote/public URL for the avatar (e.g., Firebase Storage)
    val tags: List<String> = emptyList(),
    val bio: String? = null
)
