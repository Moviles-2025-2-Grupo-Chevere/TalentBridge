package com.example.talent_bridge_kt.data.local.dao

import androidx.room.*
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity

@Dao
interface ProjectDao {

    @Query("SELECT * FROM saved_projects")
    suspend fun getAll(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("DELETE FROM saved_projects WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM saved_projects WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ProjectEntity?
}
