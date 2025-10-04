package com.example.talent_bridge_kt.domain.model


data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val headline: String,
    val linkedin: String,
    val mobileNumber: String,
    val photoUrl: String,
    val location: String,
    val skills: List<String>,
    val projects: List<String>,
    val isPublic: Boolean,
    val description: String

)
