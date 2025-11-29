package com.example.talent_bridge_kt.data.repository

import android.content.Context
import androidx.room.Room
import com.example.talent_bridge_kt.data.local.AppDatabase
import com.example.talent_bridge_kt.data.local.dao.PendingContactReviewDao
import com.example.talent_bridge_kt.data.local.entities.PendingContactReviewEntity

class ContactReviewLocalRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "projects_db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val dao: PendingContactReviewDao = db.pendingContactReviewDao()

    suspend fun enqueue(requestId: String, reviewTimeMs: Long) {
        dao.insert(
            PendingContactReviewEntity(
                requestId = requestId,
                reviewTimeMs = reviewTimeMs
            )
        )
    }

    suspend fun list(): List<PendingContactReviewEntity> = dao.getAll()

    suspend fun remove(id: Long) = dao.deleteById(id)

    suspend fun clear() = dao.clear()
}


