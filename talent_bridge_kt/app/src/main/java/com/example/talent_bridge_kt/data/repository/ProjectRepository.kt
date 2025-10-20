package com.example.talent_bridge_kt.data.repository

import android.content.Context
import androidx.room.Room
import com.example.talent_bridge_kt.data.local.AppDatabase
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProjectRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "projects_db"
    ).build()

    private val projectDao = db.projectDao()

    suspend fun saveProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.insert(project)
    }

    suspend fun removeProject(id: String) = withContext(Dispatchers.IO) {
        projectDao.deleteById(id)
    }

    suspend fun getSavedProjects(): List<ProjectEntity> = withContext(Dispatchers.IO) {
        projectDao.getAll()
    }

    suspend fun isProjectSaved(id: String): Boolean = withContext(Dispatchers.IO) {
        projectDao.findById(id) != null
    }
}
