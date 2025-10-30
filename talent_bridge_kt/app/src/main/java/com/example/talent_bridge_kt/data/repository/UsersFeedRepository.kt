package com.example.talent_bridge_kt.data.repository

import android.content.Context
import androidx.room.Room
import com.example.talent_bridge_kt.core.conectivity.ConectivityObserver
import com.example.talent_bridge_kt.data.local.AppDatabase
import com.example.talent_bridge_kt.data.local.dao.FeedStudentDao
import com.example.talent_bridge_kt.data.local.entities.FeedStudentEntity
import com.example.talent_bridge_kt.domain.model.StudentListItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UsersFeedRepository(
    private val context: Context,
    private val connectivityObserver: ConectivityObserver,
    private val dbFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "projects_db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val feedDao: FeedStudentDao = db.feedStudentDao()

    fun getStudents(): Flow<List<StudentListItem>> = feedDao.getCachedStudents().map { cached ->
        cached.map { it.toDomain() }
    }

    suspend fun refresh(): Result<List<StudentListItem>> = withContext(Dispatchers.IO) {
        try {
            val connected = connectivityObserver.observe().first()
            if (connected) {
                // Obtener todos los estudiantes públicos
                val snap = dbFirestore.collection("users")
                    .whereEqualTo("isPublic", true)
                    .get()
                    .await()

                val all = snap.documents.mapNotNull { d ->
                    val uid = d.id
                    val name = (d.getString("displayName") ?: "").ifBlank { "Student" }
                    val avatar = d.getString("avatarUrl")
                    val headline = d.getString("headline")
                    val bio = d.getString("bio")
                    @Suppress("UNCHECKED_CAST")
                    val skills = (d.get("skillsOrTopics") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    StudentListItem(
                        uid = uid,
                        displayName = name,
                        avatarUrl = avatar,
                        headline = headline,
                        bio = bio,
                        skillsOrTopics = skills
                    )
                }

                // Cachear solo 5
                val entities = all.take(5).map { it.toEntity() }
                feedDao.insertAll(entities)
                feedDao.cleanOldCache()

                Result.success(all)
            } else {
                val cached = feedDao.getCachedStudents().first()
                if (cached.isNotEmpty()) Result.success(cached.map { it.toDomain() })
                else Result.failure(Exception("No internet connection and no cached students"))
            }
        } catch (e: Exception) {
            try {
                val cached = feedDao.getCachedStudents().first()
                if (cached.isNotEmpty()) Result.success(cached.map { it.toDomain() }) else Result.failure(e)
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun StudentListItem.toEntity() = FeedStudentEntity(
        uid = uid,
        displayName = displayName,
        avatarUrl = avatarUrl,
        headline = headline,
        bio = bio,
        skillsCsv = skillsOrTopics.joinToString(","),
        cachedAt = System.currentTimeMillis()
    )

    private fun FeedStudentEntity.toDomain() = StudentListItem(
        uid = uid,
        displayName = displayName,
        avatarUrl = avatarUrl,
        headline = headline,
        bio = bio,
        skillsOrTopics = if (skillsCsv.isBlank()) emptyList() else skillsCsv.split(",").map { it.trim() }
    )
}
