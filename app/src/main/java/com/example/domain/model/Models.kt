package com.example.domain.model

import android.graphics.drawable.Drawable
import android.os.UserHandle
import androidx.compose.runtime.Immutable

@Immutable
data class AppItem(
    val packageName: String,
    val activityName: String = "",
    val label: String,
    val icon: Drawable? = null,
    val userHandle: UserHandle? = null,
    val isWorkProfile: Boolean = false,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val launchCount: Int = 0,
    val lastLaunchedTimestamp: Long = 0L,
    val installedTimestamp: Long = 0L
) {
    val uniqueKey: String
        get() = "${packageName}_${activityName}_${userHandle?.hashCode() ?: 0}_${if (isWorkProfile) "work" else "main"}"
}

enum class OutputLineType {
    NORMAL,
    COMMAND_ECHO,
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    APP_LINK,
    ASCII_ART,
    HEADER
}

@Immutable
data class OutputLine(
    val text: String,
    val type: OutputLineType = OutputLineType.NORMAL,
    val appPackage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AliasItem(
    val name: String,
    val command: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class NoteItem(
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TodoItem(
    val id: Long = 0,
    val task: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
