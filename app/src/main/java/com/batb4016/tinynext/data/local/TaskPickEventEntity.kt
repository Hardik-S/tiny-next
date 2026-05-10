package com.batb4016.tinynext.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_pick_events",
    indices = [
        Index("taskId"),
        Index("pickedAt"),
        Index("action"),
    ],
)
data class TaskPickEventEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val pickedAt: Long,
    val action: String,
)
