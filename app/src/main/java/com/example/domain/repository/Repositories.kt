package com.example.domain.repository

import android.content.Context
import android.os.UserHandle
import com.example.core.ui.theme.TerminalThemeType
import com.example.data.local.datastore.ShellPreferences
import com.example.domain.model.AliasItem
import com.example.domain.model.AppItem
import com.example.domain.model.NoteItem
import com.example.domain.model.TodoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AppRepository {
    val appsFlow: StateFlow<List<AppItem>>

    suspend fun refreshApps()
    suspend fun launchApp(packageName: String, userHandle: UserHandle? = null, activityName: String? = null): Result<Unit>
    suspend fun openAppInfo(packageName: String): Result<Unit>
    suspend fun uninstallApp(packageName: String): Result<Unit>
    suspend fun openPlayStore(packageName: String): Result<Unit>
    suspend fun setFavorite(packageName: String, isFavorite: Boolean)
    suspend fun setHidden(packageName: String, isHidden: Boolean)
    suspend fun setLabelOverride(packageName: String, customLabel: String?)
    suspend fun findApp(query: String): AppItem?
    suspend fun recordLaunch(packageName: String)
    suspend fun getAppShortcuts(packageName: String): List<String>
}

interface SettingsRepository {
    val preferencesFlow: Flow<ShellPreferences>

    suspend fun setTheme(theme: TerminalThemeType)
    suspend fun setFontSize(size: Float)
    suspend fun setFontName(fontName: String)
    suspend fun setPrompt(user: String, host: String)
    suspend fun setBootAnimation(enabled: Boolean)
    suspend fun setAutoLaunch(enabled: Boolean)
    suspend fun setNeofetchOnBoot(enabled: Boolean)
    suspend fun setCrtScanlines(enabled: Boolean)
    suspend fun setScrollbackLimit(limit: Int)
    suspend fun setDefaultMode(mode: String)
    suspend fun setSearchEngine(engine: String)
    suspend fun setWallpaperDim(dim: Float)
    suspend fun setGestures(swipeUp: String, swipeDown: String, doubleTap: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
}

interface ProductivityRepository {
    val notesFlow: Flow<List<NoteItem>>
    val todosFlow: Flow<List<TodoItem>>
    val aliasesFlow: Flow<List<AliasItem>>

    suspend fun addNote(title: String, content: String): Long
    suspend fun deleteNote(id: Long)
    suspend fun getNotes(): List<NoteItem>

    suspend fun addTodo(task: String): Long
    suspend fun setTodoCompleted(id: Long, completed: Boolean)
    suspend fun deleteTodo(id: Long)
    suspend fun deleteCompletedTodos()

    suspend fun setAlias(name: String, command: String)
    suspend fun getAlias(name: String): String?
    suspend fun removeAlias(name: String)
    suspend fun getAllAliases(): Map<String, String>

    suspend fun addHistory(command: String)
    suspend fun getRecentHistory(limit: Int): List<String>
    suspend fun clearHistory()
}
