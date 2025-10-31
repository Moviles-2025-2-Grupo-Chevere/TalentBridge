package com.example.talent_bridge_kt.data.repository

import android.content.Context
import androidx.room.Room
import com.example.talent_bridge_kt.data.local.AppDatabase
import com.example.talent_bridge_kt.data.local.dao.PendingApplicationDao
import com.example.talent_bridge_kt.data.local.entities.PendingApplicationEntity

class ApplicationsLocalRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "projects_db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val dao: PendingApplicationDao = db.pendingApplicationDao()

    suspend fun enqueue(projectId: String, createdById: String, projectTitle: String) {
        dao.insert(PendingApplicationEntity(
            projectId = projectId,
            createdById = createdById,
            projectTitle = projectTitle
        ))
    }

    suspend fun list(): List<PendingApplicationEntity> = dao.getAll()

    suspend fun remove(id: Long) = dao.deleteById(id)

    suspend fun clear() = dao.clear()
}
