package com.batb4016.tinynext.data

import com.batb4016.tinynext.data.local.CategoryEntity
import com.batb4016.tinynext.data.local.CompletionEventEntity
import com.batb4016.tinynext.data.local.DefaultCategories
import com.batb4016.tinynext.data.local.TaskEntity
import com.batb4016.tinynext.data.local.TaskPickEventEntity
import com.batb4016.tinynext.data.local.TinyNextDatabase
import com.batb4016.tinynext.domain.Recurrence
import com.batb4016.tinynext.domain.TaskPickOutcome
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class TinyNextRepository(
    private val database: TinyNextDatabase,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    val tasks: Flow<List<TaskEntity>> = database.taskDao().observeActive()
    val categories: Flow<List<CategoryEntity>> = database.categoryDao().observeAll()
    val completions: Flow<List<CompletionEventEntity>> = database.completionEventDao().observeAll()
    val pickEvents: Flow<List<TaskPickEventEntity>> = database.taskPickEventDao().observeRecent()

    suspend fun ensureDefaultCategories() {
        if (database.categoryDao().getAll().isEmpty()) {
            database.categoryDao().insertAll(DefaultCategories.values)
        }
    }

    suspend fun addTask(
        title: String,
        categoryId: String,
        estimateMinutes: Int,
        isStarred: Boolean,
        recurrence: String,
        now: Long = System.currentTimeMillis(),
    ) {
        database.taskDao().insert(
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                categoryId = categoryId,
                estimateMinutes = estimateMinutes,
                isStarred = isStarred,
                recurrence = recurrence.ifBlank { Recurrence.NONE.name },
                createdAt = now,
                updatedAt = now,
                archivedAt = null,
                lastCompletedAt = null,
                lastPickedAt = null,
                snoozedUntil = null,
                totalCompletionCount = 0,
                totalSkipCount = 0,
            )
        )
    }

    suspend fun updateTask(
        id: String,
        title: String,
        categoryId: String,
        estimateMinutes: Int,
        isStarred: Boolean,
        recurrence: String,
        now: Long = System.currentTimeMillis(),
    ) {
        val existing = database.taskDao().getById(id) ?: return
        database.taskDao().update(
            existing.copy(
                title = title,
                categoryId = categoryId,
                estimateMinutes = estimateMinutes,
                isStarred = isStarred,
                recurrence = recurrence,
                updatedAt = now,
            )
        )
    }

    suspend fun addSampleTasks() {
        ensureDefaultCategories()
        val now = System.currentTimeMillis()
        listOf(
            "Drink water",
            "Clear one surface",
            "Reply to one message",
            "Review today's list",
            "Take out trash",
            "Prepare tomorrow's first step",
        ).forEachIndexed { index, title ->
            addTask(
                title = title,
                categoryId = if (index == 4) "home" else "quick",
                estimateMinutes = if (index == 5) 15 else 5,
                isStarred = index == 0,
                recurrence = if (index == 0) Recurrence.DAILY.name else Recurrence.NONE.name,
                now = now + index,
            )
        }
    }

    suspend fun markPicked(taskId: String, action: TaskPickOutcome, now: Long = System.currentTimeMillis()) {
        database.taskDao().markPicked(taskId, now)
        database.taskPickEventDao().insert(
            TaskPickEventEntity(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                pickedAt = now,
                action = action.storageValue,
            )
        )
    }

    suspend fun markDone(taskId: String, now: Long = System.currentTimeMillis()) {
        database.taskDao().markCompleted(taskId, now)
        database.completionEventDao().insert(
            CompletionEventEntity(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                completedAt = now,
                localDate = Instant.ofEpochMilli(now).atZone(zoneId).toLocalDate().toString(),
            )
        )
        markPicked(taskId, TaskPickOutcome.DONE, now)
    }

    suspend fun skip(taskId: String, now: Long = System.currentTimeMillis()) {
        database.taskDao().markSkipped(taskId, now)
        markPicked(taskId, TaskPickOutcome.SKIPPED, now)
    }

    suspend fun snooze(taskId: String, now: Long = System.currentTimeMillis()) {
        database.taskDao().setSnoozedUntil(taskId, now + THREE_HOURS_MILLIS, now)
        markPicked(taskId, TaskPickOutcome.SNOOZED, now)
    }

    suspend fun archive(taskId: String, now: Long = System.currentTimeMillis()) {
        database.taskDao().setArchived(taskId, now, now)
    }

    suspend fun delete(task: TaskEntity) {
        database.completionEventDao().deleteForTask(task.id)
        database.taskDao().delete(task)
    }

    suspend fun restoreTask(task: TaskEntity) {
        database.taskDao().insert(task)
    }

    suspend fun deleteAllLocalData() {
        database.taskPickEventDao().deleteAll()
        database.completionEventDao().deleteAll()
        database.taskDao().deleteAll()
        ensureDefaultCategories()
    }

    companion object {
        private const val THREE_HOURS_MILLIS = 3L * 60L * 60L * 1000L
    }
}
