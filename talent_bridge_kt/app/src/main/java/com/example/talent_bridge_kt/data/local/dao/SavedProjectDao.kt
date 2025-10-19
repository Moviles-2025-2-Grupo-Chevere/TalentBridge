// data/local/dao/SavedProjectDao.kt
package com.example.talent_bridge_kt.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talent_bridge_kt.data.local.entity.SavedProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedProjectEntity)

    @Query("DELETE FROM saved_projects WHERE id = :projectId")
    suspend fun deleteById(projectId: String)

    @Query("SELECT * FROM saved_projects ORDER BY createdAtMs DESC NULLS LAST, title ASC")
    fun getAll(): Flow<List<SavedProjectEntity>>

    @Query("SELECT id FROM saved_projects")
    fun getAllIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_projects WHERE id = :projectId)")
    fun isSaved(projectId: String): Flow<Boolean>
}
