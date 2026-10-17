package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.db.entity.CommandAliasEntity
import com.example.data.local.db.entity.CommandHistoryEntity
import com.example.data.local.db.entity.NoteEntity
import com.example.data.local.db.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AliasDao {
    @Query("SELECT * FROM command_alias ORDER BY aliasName ASC")
    fun getAllAliases(): Flow<List<CommandAliasEntity>>

    @Query("SELECT * FROM command_alias WHERE aliasName = :name LIMIT 1")
    suspend fun getAlias(name: String): CommandAliasEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: CommandAliasEntity)

    @Query("DELETE FROM command_alias WHERE aliasName = :name")
    suspend fun deleteAlias(name: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM command_history ORDER BY id DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<CommandHistoryEntity>>

    @Query("SELECT * FROM command_history ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentHistoryList(limit: Int): List<CommandHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CommandHistoryEntity)

    @Query("DELETE FROM command_history")
    suspend fun clearHistory()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM note ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM note")
    suspend fun clearAllNotes()
}

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todo WHERE id = :id LIMIT 1")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Query("UPDATE todo SET isCompleted = :completed WHERE id = :id")
    suspend fun updateStatus(id: Long, completed: Boolean)

    @Query("DELETE FROM todo WHERE id = :id")
    suspend fun deleteTodoById(id: Long)

    @Query("DELETE FROM todo WHERE isCompleted = 1")
    suspend fun deleteCompleted()
}
