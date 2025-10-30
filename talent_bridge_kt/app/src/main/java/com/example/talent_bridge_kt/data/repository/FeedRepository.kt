package com.example.talent_bridge_kt.data.repository

import android.content.Context
import androidx.room.Room
import com.example.talent_bridge_kt.core.conectivity.ConectivityObserver
import com.example.talent_bridge_kt.data.local.AppDatabase
import com.example.talent_bridge_kt.data.local.dao.FeedProjectDao
import com.example.talent_bridge_kt.data.local.entities.FeedProjectEntity
import com.example.talent_bridge_kt.domain.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FeedRepository(
    private val context: Context,
    private val firestoreRepo: FirestoreProjectsRepository = FirestoreProjectsRepository(),
    private val connectivityObserver: ConectivityObserver
) {
    
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "projects_db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val feedDao: FeedProjectDao = db.feedProjectDao()

    /**
     * Estrategia: Cache First con fallback a network
     * 1. Si hay internet: muestra TODOS los proyectos de Firestore
     * 2. Si no hay internet: muestra solo cache (5 proyectos)
     */
    fun getProjects(): Flow<List<Project>> = feedDao.getCachedProjects().map { cachedProjects ->
        cachedProjects.map { it.toDomain() }
    }

    /**
     * Refresca los proyectos:
     * - Si hay internet: obtiene TODOS los proyectos de Firestore y actualiza cache
     * - Si no hay internet: solo muestra cache existente
     */
    suspend fun refreshProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            val isConnected = connectivityObserver.observe().first()
            
            if (isConnected) {
                // Hay internet: obtener TODOS los proyectos de Firestore
                val freshProjects = firestoreRepo.fetchAllProjects()
                
                // Actualizar cache con solo los primeros 5 para offline
                val entities = freshProjects.take(5).map { it.toFeedEntity() }
                feedDao.insertProjects(entities)
                feedDao.cleanOldCache()
                
                // Devolver TODOS los proyectos (no solo los del cache)
                Result.success(freshProjects)
            } else {
                // No hay internet: devolver solo cache existente
                val cachedProjects = feedDao.getCachedProjects().first()
                if (cachedProjects.isNotEmpty()) {
                    Result.success(cachedProjects.map { it.toDomain() })
                } else {
                    Result.failure(Exception("No internet connection and no cached projects"))
                }
            }
        } catch (e: Exception) {
            // En caso de error, intentar devolver cache
            try {
                val cachedProjects = feedDao.getCachedProjects().first()
                if (cachedProjects.isNotEmpty()) {
                    Result.success(cachedProjects.map { it.toDomain() })
                } else {
                    Result.failure(e)
                }
            } catch (cacheError: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Verifica si hay proyectos en cache
     */
    suspend fun hasCachedProjects(): Boolean = withContext(Dispatchers.IO) {
        feedDao.getCacheCount() > 0
    }

    /**
     * Limpia el cache (útil para testing o reset)
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        feedDao.clearCache()
    }

    // Mappers
    private fun Project.toFeedEntity() = FeedProjectEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        description = description,
        skills = skills.joinToString(","),
        imgUrl = imgUrl,
        createdAt = createdAt?.toDate()?.time,
        createdById = createdById,
        cachedAt = System.currentTimeMillis()
    )

    private fun FeedProjectEntity.toDomain() = Project(
        id = id,
        title = title,
        subtitle = subtitle,
        description = description,
        skills = if (skills.isBlank()) emptyList() else skills.split(",").map { it.trim() },
        imgUrl = imgUrl,
        createdAt = createdAt?.let { com.google.firebase.Timestamp(it / 1000, ((it % 1000) * 1000000).toInt()) },
        createdById = createdById
    )
}
