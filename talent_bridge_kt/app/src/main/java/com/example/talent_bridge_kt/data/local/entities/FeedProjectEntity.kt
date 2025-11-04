package com.example.talent_bridge_kt.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_projects_cache")
data class FeedProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String?,
    val description: String,
    val skills: String, // CSV format
    val imgUrl: String?,
    val createdAt: Long?, // timestamp in milliseconds
    val createdById: String,
    val cachedAt: Long = System.currentTimeMillis() // when this was cached
)
