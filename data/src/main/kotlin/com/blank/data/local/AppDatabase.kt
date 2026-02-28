package com.blank.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blank.data.local.dao.ArticleDao
import com.blank.data.local.entity.ArticleEntity

@Database(
    entities = [ArticleEntity::class, RemoteKeyEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
