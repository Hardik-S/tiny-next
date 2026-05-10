package com.batb4016.tinynext.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completion_events",
    indices = [
        Index("taskId"),
        Index("completedAt"),
        Index("localDate"),
    ],
)
data class CompletionEventEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val completedAt: Long,
    val localDate: String,
)
