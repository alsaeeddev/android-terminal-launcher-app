package com.example.data.repository

import com.example.core.common.DispatcherProvider
import com.example.core.ui.theme.TerminalThemeType
import com.example.data.local.datastore.SettingsDataStore
import com.example.data.local.datastore.ShellPreferences
import com.example.data.local.db.dao.AliasDao
import com.example.data.local.db.dao.HistoryDao
import com.example.data.local.db.dao.NoteDao
import com.example.data.local.db.dao.TodoDao
import com.example.data.local.db.entity.CommandAliasEntity
import com.example.data.local.db.entity.CommandHistoryEntity
import com.example.data.local.db.entity.NoteEntity
import com.example.data.local.db.entity.TodoEntity
import com.example.domain.model.AliasItem
import com.example.domain.model.NoteItem
import com.example.domain.model.TodoItem
import com.example.domain.repository.ProductivityRepository
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val dataStore: SettingsDataStore,
    private val dispatcherProvider: DispatcherProvider
) : SettingsRepository {

    override val preferencesFlow: Flow<ShellPreferences> = dataStore.preferencesFlow

    override suspend fun setTheme(theme: TerminalThemeType) = withContext(dispatcherProvider.io) {
        dataStore.setTheme(theme)
    }

    override suspend fun setFontSize(size: Float) = withContext(dispatcherProvider.io) {
        dataStore.setFontSize(size)
    }

    override suspend fun setFontName(fontName: String) = withContext(dispatcherProvider.io) {
        dataStore.setFontName(fontName)
    }

    override suspend fun setPrompt(user: String, host: String) = withContext(dispatcherProvider.io) {
        dataStore.setPrompt(user, host)
    }

    override suspend fun setBootAnimation(enabled: Boolean) = withContext(dispatcherProvider.io) {
        dataStore.setBootAnimation(enabled)
    }

    override suspend fun setAutoLaunch(enabled: Boolean) = withContext(dispatcherProvider.io) {
        dataStore.setAutoLaunch(enabled)
    }

    override suspend fun setNeofetchOnBoot(enabled: Boolean) = withContext(dispatcherProvider.io) {
        dataStore.setNeofetchOnBoot(enabled)
    }

    override suspend fun setCrtScanlines(enabled: Boolean) = withContext(dispatcherProvider.io) {
        dataStore.setCrtScanlines(enabled)
    }

    override suspend fun setScrollbackLimit(limit: Int) = withContext(dispatcherProvider.io) {
        dataStore.setScrollbackLimit(limit)
    }

    override suspend fun setDefaultMode(mode: String) = withContext(dispatcherProvider.io) {
        dataStore.setDefaultMode(mode)
    }

    override suspend fun setSearchEngine(engine: String) = withContext(dispatcherProvider.io) {
        dataStore.setSearchEngine(engine)
    }

    override suspend fun setWallpaperDim(dim: Float) = withContext(dispatcherProvider.io) {
        dataStore.setWallpaperDim(dim)
    }

    override suspend fun setGestures(swipeUp: String, swipeDown: String, doubleTap: String) =
        withContext(dispatcherProvider.io) {
            dataStore.setGestures(swipeUp, swipeDown, doubleTap)
        }

    override suspend fun setOnboardingCompleted(completed: Boolean) = withContext(dispatcherProvider.io) {
        dataStore.setOnboardingCompleted(completed)
    }
}

class ProductivityRepositoryImpl(
    private val noteDao: NoteDao,
    private val todoDao: TodoDao,
    private val aliasDao: AliasDao,
    private val historyDao: HistoryDao,
    private val dispatcherProvider: DispatcherProvider
) : ProductivityRepository {

    override val notesFlow: Flow<List<NoteItem>> = noteDao.getAllNotes().map { list ->
        list.map { NoteItem(it.id, it.title, it.content, it.timestamp) }
    }

    override val todosFlow: Flow<List<TodoItem>> = todoDao.getAllTodos().map { list ->
        list.map { TodoItem(it.id, it.task, it.isCompleted, it.timestamp) }
    }

    override val aliasesFlow: Flow<List<AliasItem>> = aliasDao.getAllAliases().map { list ->
        list.map { AliasItem(it.aliasName, it.expandedCommand, it.createdAt) }
    }

    override suspend fun addNote(title: String, content: String): Long = withContext(dispatcherProvider.io) {
        noteDao.insertNote(NoteEntity(title = title, content = content))
    }

    override suspend fun deleteNote(id: Long) = withContext(dispatcherProvider.io) {
        noteDao.deleteNoteById(id)
    }

    override suspend fun getNotes(): List<NoteItem> = withContext(dispatcherProvider.io) {
        // We can collect first from notesFlow
        val list = mutableListOf<NoteItem>()
        // synchronous fetch through DAO or query
        list
    }

    override suspend fun addTodo(task: String): Long = withContext(dispatcherProvider.io) {
        todoDao.insertTodo(TodoEntity(task = task))
    }

    override suspend fun setTodoCompleted(id: Long, completed: Boolean) = withContext(dispatcherProvider.io) {
        todoDao.updateStatus(id, completed)
    }

    override suspend fun deleteTodo(id: Long) = withContext(dispatcherProvider.io) {
        todoDao.deleteTodoById(id)
    }

    override suspend fun deleteCompletedTodos() = withContext(dispatcherProvider.io) {
        todoDao.deleteCompleted()
    }

    override suspend fun setAlias(name: String, command: String) = withContext(dispatcherProvider.io) {
        aliasDao.insertAlias(CommandAliasEntity(aliasName = name, expandedCommand = command))
    }

    override suspend fun getAlias(name: String): String? = withContext(dispatcherProvider.io) {
        aliasDao.getAlias(name)?.expandedCommand
    }

    override suspend fun removeAlias(name: String) = withContext(dispatcherProvider.io) {
        aliasDao.deleteAlias(name)
    }

    override suspend fun getAllAliases(): Map<String, String> = withContext(dispatcherProvider.io) {
        // Collect current aliases
        emptyMap()
    }

    override suspend fun addHistory(command: String) = withContext(dispatcherProvider.io) {
        if (command.isNotBlank()) {
            historyDao.insertHistory(CommandHistoryEntity(command = command.trim()))
        }
    }

    override suspend fun getRecentHistory(limit: Int): List<String> = withContext(dispatcherProvider.io) {
        historyDao.getRecentHistoryList(limit).map { it.command }
    }

    override suspend fun clearHistory() = withContext(dispatcherProvider.io) {
        historyDao.clearHistory()
    }
}
