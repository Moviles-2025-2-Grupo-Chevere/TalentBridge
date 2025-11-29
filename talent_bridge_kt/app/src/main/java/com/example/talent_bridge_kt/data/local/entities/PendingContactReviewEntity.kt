package com.example.talent_bridge_kt.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_contact_reviews")
data class PendingContactReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: String,
    val reviewTimeMs: Long,
    val createdAt: Long = System.currentTimeMillis()
)


