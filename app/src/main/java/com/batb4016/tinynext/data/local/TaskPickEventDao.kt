package com.batb4016.tinynext.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskPickEventDao {
    @Query("SELECT * FROM task_pick_events ORDER BY pickedAt DESC")
    fun observeRecent(): Flow<List<TaskPickEventEntity>>

    @Query("SELECT * FROM task_pick_events ORDER BY pickedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<TaskPickEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: TaskPickEventEntity)

    @Query("DELETE FROM task_pick_events WHERE pickedAt < :beforeEpochMillis")
    suspend fun deleteOlderThan(beforeEpochMillis: Long)

    @Query("DELETE FROM task_pick_events")
    suspend fun deleteAll()
}
