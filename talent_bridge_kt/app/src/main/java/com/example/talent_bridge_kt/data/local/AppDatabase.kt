package com.example.talent_bridge_kt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.talent_bridge_kt.data.local.dao.ProjectDao
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity

@Database(entities = [ProjectEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}
