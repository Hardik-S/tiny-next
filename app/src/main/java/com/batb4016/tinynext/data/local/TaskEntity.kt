package com.batb4016.tinynext.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index("categoryId"),
        Index("archivedAt"),
        Index("snoozedUntil"),
    ],
)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val categoryId: String,
    val estimateMinutes: Int,
    val isStarred: Boolean,
    val recurrence: String,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long?,
    val lastCompletedAt: Long?,
    val lastPickedAt: Long?,
    val snoozedUntil: Long?,
    val totalCompletionCount: Int,
    val totalSkipCount: Int,
)
