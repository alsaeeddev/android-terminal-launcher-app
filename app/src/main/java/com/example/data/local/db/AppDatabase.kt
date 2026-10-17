package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.db.dao.AliasDao
import com.example.data.local.db.dao.AppDao
import com.example.data.local.db.dao.HistoryDao
import com.example.data.local.db.dao.NoteDao
import com.example.data.local.db.dao.TodoDao
import com.example.data.local.db.entity.AppLabelOverrideEntity
import com.example.data.local.db.entity.AppUsageEntity
import com.example.data.local.db.entity.CommandAliasEntity
import com.example.data.local.db.entity.CommandHistoryEntity
import com.example.data.local.db.entity.FavoriteAppEntity
import com.example.data.local.db.entity.HiddenAppEntity
import com.example.data.local.db.entity.NoteEntity
import com.example.data.local.db.entity.TodoEntity

@Database(
    entities = [
        AppUsageEntity::class,
        FavoriteAppEntity::class,
        HiddenAppEntity::class,
        AppLabelOverrideEntity::class,
        CommandAliasEntity::class,
        CommandHistoryEntity::class,
        NoteEntity::class,
        TodoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun aliasDao(): AliasDao
    abstract fun historyDao(): HistoryDao
    abstract fun noteDao(): NoteDao
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shell_home.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
