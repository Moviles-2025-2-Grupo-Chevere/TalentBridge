package com.example.talent_bridge_kt.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String?,
    val description: String,
    val skills: String,
    val imgUrl: String?,
    val createdAt: String?,
    val createdById: String,
)
