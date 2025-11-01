package com.example.talent_bridge_kt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talent_bridge_kt.data.local.entities.FeedStudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedStudentDao {

    @Query("SELECT * FROM feed_students_cache ORDER BY cachedAt DESC LIMIT 5")
    fun getCachedStudents(): Flow<List<FeedStudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FeedStudentEntity>)

    @Query("DELETE FROM feed_students_cache WHERE uid NOT IN (SELECT uid FROM feed_students_cache ORDER BY cachedAt DESC LIMIT 5)")
    suspend fun cleanOldCache()

    @Query("DELETE FROM feed_students_cache")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM feed_students_cache")
    suspend fun count(): Int

    @Query("SELECT * FROM feed_students_cache ORDER BY cachedAt DESC")
    suspend fun getAllCachedStudents(): List<FeedStudentEntity>
}
