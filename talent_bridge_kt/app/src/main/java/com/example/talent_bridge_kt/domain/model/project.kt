package com.example.talent_bridge_kt.domain.model

import java.util.UUID

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val skills: List<String> = emptyList(),
    val imgUrl: String? = null
)
