// data/local/entities/PendingProjectEntity.kt
package com.example.talent_bridge_kt.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_projects")
data class PendingProjectEntity(
    @PrimaryKey val localId: String,
    val userId: String,
    val title: String,
    val description: String,
    val skillsCsv: String,
    val imageUri: String?,
    val createdAt: Long
)
