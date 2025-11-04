package com.example.talent_bridge_kt.domain.model

import com.google.firebase.Timestamp
import java.util.UUID

data class Project(
    val id: String,
    val title: String,
    val subtitle: String?,
    val description: String,
    val skills: List<String>,
    val imgUrl: String?,
    val createdAt: com.google.firebase.Timestamp?,
    val createdById: String
)

