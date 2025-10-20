package com.example.talent_bridge_kt.data.local.dao

import androidx.room.*
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    // Trae SOLO los guardados del usuario
    @Query("SELECT * FROM saved_projects WHERE userId = :userId")
    fun getAllForUser(userId: String): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    // Borra por id pero SOLO si pertenece al usuario
    @Query("DELETE FROM saved_projects WHERE id = :id AND userId = :userId")
    suspend fun deleteById(id: String, userId: String)

    // Busca un guardado de ese usuario
    @Query("SELECT * FROM saved_projects WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun findById(id: String, userId: String): ProjectEntity?
}
