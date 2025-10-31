package com.example.talent_bridge_kt.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.talent_bridge_kt.data.local.dao.FeedProjectDao
import com.example.talent_bridge_kt.data.local.dao.FeedStudentDao
import com.example.talent_bridge_kt.data.local.dao.ProjectDao
import com.example.talent_bridge_kt.data.local.entities.FeedProjectEntity
import com.example.talent_bridge_kt.data.local.entities.FeedStudentEntity
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity
import com.example.talent_bridge_kt.data.local.dao.PendingProjectDao
import com.example.talent_bridge_kt.data.local.dao.ProjectDao
import com.example.talent_bridge_kt.data.local.entities.PendingProjectEntity
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity

@Database(entities = [ProjectEntity::class, FeedProjectEntity::class, FeedStudentEntity::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun feedProjectDao(): FeedProjectDao
    abstract fun feedStudentDao(): FeedStudentDao
    abstract fun projectDao(): ProjectDao
    abstract fun pendingProjectDao(): PendingProjectDao


    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "talentbridge.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
