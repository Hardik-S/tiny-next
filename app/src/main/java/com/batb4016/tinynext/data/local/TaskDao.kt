package com.batb4016.tinynext.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isStarred DESC, updatedAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE archivedAt IS NULL ORDER BY isStarred DESC, updatedAt DESC")
    fun observeActive(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET archivedAt = :archivedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchived(id: String, archivedAt: Long?, updatedAt: Long)

    @Query("UPDATE tasks SET snoozedUntil = :snoozedUntil, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setSnoozedUntil(id: String, snoozedUntil: Long?, updatedAt: Long)

    @Query("UPDATE tasks SET lastPickedAt = :pickedAt, updatedAt = :pickedAt WHERE id = :id")
    suspend fun markPicked(id: String, pickedAt: Long)

    @Query(
        """
        UPDATE tasks
        SET totalCompletionCount = totalCompletionCount + 1,
            lastCompletedAt = :completedAt,
            updatedAt = :completedAt,
            snoozedUntil = NULL
        WHERE id = :id
        """,
    )
    suspend fun markCompleted(id: String, completedAt: Long)

    @Query("UPDATE tasks SET totalSkipCount = totalSkipCount + 1, updatedAt = :skippedAt WHERE id = :id")
    suspend fun markSkipped(id: String, skippedAt: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
