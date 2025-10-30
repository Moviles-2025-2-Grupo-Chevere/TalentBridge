package com.example.talent_bridge_kt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.talent_bridge_kt.data.local.dao.FeedProjectDao
import com.example.talent_bridge_kt.data.local.dao.ProjectDao
import com.example.talent_bridge_kt.data.local.dao.PendingApplicationDao
import com.example.talent_bridge_kt.data.local.entities.FeedProjectEntity
import com.example.talent_bridge_kt.data.local.entities.ProjectEntity
import com.example.talent_bridge_kt.data.local.entities.PendingApplicationEntity

@Database(
    entities = [
        ProjectEntity::class,
        FeedProjectEntity::class,
        PendingApplicationEntity::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun feedProjectDao(): FeedProjectDao
    abstract fun pendingApplicationDao(): PendingApplicationDao
}
