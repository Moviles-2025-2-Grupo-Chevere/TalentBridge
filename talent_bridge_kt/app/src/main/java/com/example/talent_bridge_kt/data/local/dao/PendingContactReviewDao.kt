package com.example.talent_bridge_kt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talent_bridge_kt.data.local.entities.PendingContactReviewEntity

@Dao
interface PendingContactReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PendingContactReviewEntity): Long

    @Query("SELECT * FROM pending_contact_reviews ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingContactReviewEntity>

    @Query("DELETE FROM pending_contact_reviews WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pending_contact_reviews")
    suspend fun clear()
}


