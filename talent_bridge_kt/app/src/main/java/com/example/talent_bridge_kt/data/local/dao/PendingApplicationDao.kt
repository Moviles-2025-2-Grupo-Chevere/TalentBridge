package com.example.talent_bridge_kt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talent_bridge_kt.data.local.entities.PendingApplicationEntity

@Dao
interface PendingApplicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PendingApplicationEntity): Long

    @Query("SELECT * FROM pending_applications ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingApplicationEntity>

    @Query("DELETE FROM pending_applications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pending_applications")
    suspend fun clear()
}
