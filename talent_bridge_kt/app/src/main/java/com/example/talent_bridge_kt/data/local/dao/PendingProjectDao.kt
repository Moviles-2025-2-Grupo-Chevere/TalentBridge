package com.example.talent_bridge_kt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talent_bridge_kt.data.local.entities.PendingProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPending(project: PendingProjectEntity)

    @Query("SELECT * FROM pending_projects ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingProjectEntity>

    @Query("DELETE FROM pending_projects WHERE localId = :id")
    suspend fun deletePending(id: String)

    @Query("DELETE FROM pending_projects")
    suspend fun clearAll()
}
