package com.example.talent_bridge_kt.data.firebase.model

import com.google.firebase.Timestamp

data class FirestoreProject(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imgUrl: String? = null,
    val skills: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val createdById: String = ""
)