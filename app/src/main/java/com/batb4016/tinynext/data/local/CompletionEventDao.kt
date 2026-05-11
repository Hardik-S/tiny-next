package com.batb4016.tinynext.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionEventDao {
    @Query("SELECT * FROM completion_events ORDER BY completedAt DESC")
    fun observeAll(): Flow<List<CompletionEventEntity>>

    @Query("SELECT * FROM completion_events ORDER BY completedAt DESC")
    suspend fun getAll(): List<CompletionEventEntity>

    @Query("SELECT * FROM completion_events WHERE taskId = :taskId ORDER BY completedAt DESC")
    suspend fun getForTask(taskId: String): List<CompletionEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CompletionEventEntity)

    @Query("DELETE FROM completion_events WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: String)

    @Query("DELETE FROM completion_events")
    suspend fun deleteAll()
}
