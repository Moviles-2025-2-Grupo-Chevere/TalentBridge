package com.example.talent_bridge_kt.data.repository

import android.content.Context
import androidx.room.Room
import com.example.talent_bridge_kt.data.local.AppDatabase
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProjectRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "projects_db"
    )
        // Útil en desarrollo tras cambiar el schema; quítalo si agregas migraciones
        .fallbackToDestructiveMigration()
        .build()

    private val projectDao = db.projectDao()

    suspend fun saveProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.insert(project)
    }

    suspend fun removeProject(id: String, userId: String) = withContext(Dispatchers.IO) {
        projectDao.deleteById(id, userId)
    }

    fun getSavedProjects(userId: String): Flow<List<ProjectEntity>> =
        projectDao.getAllForUser(userId)

    suspend fun isProjectSaved(id: String, userId: String): Boolean =
        withContext(Dispatchers.IO) { projectDao.findById(id, userId) != null }
}
