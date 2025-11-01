package com.example.talent_bridge_kt.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_applications")
data class PendingApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: String,
    val createdById: String,
    val projectTitle: String,
    val createdAt: Long = System.currentTimeMillis()
)
