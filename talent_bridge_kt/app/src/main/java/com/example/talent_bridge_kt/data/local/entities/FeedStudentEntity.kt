package com.example.talent_bridge_kt.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_students_cache")
data class FeedStudentEntity(
    @PrimaryKey val uid: String,
    val displayName: String,
    val avatarUrl: String?,
    val headline: String?,
    val bio: String?,
    val skillsCsv: String,
    val cachedAt: Long = System.currentTimeMillis()
)
