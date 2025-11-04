package com.example.talent_bridge_kt.data.local.dao

import androidx.room.*
import com.example.talent_bridge_kt.data.local.entities.FeedProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedProjectDao {

    // Obtener todos los proyectos del cache (últimos 5)
    @Query("SELECT * FROM feed_projects_cache ORDER BY createdAt DESC LIMIT 5")
    fun getCachedProjects(): Flow<List<FeedProjectEntity>>

    // Insertar proyectos en cache (reemplaza existentes)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<FeedProjectEntity>)

    // Limpiar cache antiguo (mantener solo los últimos 5)
    @Query("DELETE FROM feed_projects_cache WHERE id NOT IN (SELECT id FROM feed_projects_cache ORDER BY createdAt DESC LIMIT 5)")
    suspend fun cleanOldCache()

    // Verificar si hay proyectos en cache
    @Query("SELECT COUNT(*) FROM feed_projects_cache")
    suspend fun getCacheCount(): Int

    // Limpiar todo el cache
    @Query("DELETE FROM feed_projects_cache")
    suspend fun clearCache()
}
