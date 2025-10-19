// data/local/entity/SavedProjectEntity.kt
package com.example.talent_bridge_kt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_projects")
data class SavedProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String?,
    val description: String,
    val imgUrl: String?,
    val createdAtMs: Long?,
    val createdById: String?,
    val skillsCsv: String
)
