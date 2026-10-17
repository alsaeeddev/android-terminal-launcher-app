package com.example.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val packageName: String,
    val launchCount: Int = 1,
    val lastLaunchedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_app")
data class FavoriteAppEntity(
    @PrimaryKey val packageName: String,
    val orderIndex: Int = 0
)

@Entity(tableName = "hidden_app")
data class HiddenAppEntity(
    @PrimaryKey val packageName: String,
    val hiddenTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_label_override")
data class AppLabelOverrideEntity(
    @PrimaryKey val packageName: String,
    val customLabel: String
)

@Entity(tableName = "command_alias")
data class CommandAliasEntity(
    @PrimaryKey val aliasName: String,
    val expandedCommand: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "note")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "todo")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val task: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
