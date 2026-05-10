package com.batb4016.tinynext.domain

import com.batb4016.tinynext.data.local.CompletionEventEntity
import com.batb4016.tinynext.data.local.TaskEntity
import com.batb4016.tinynext.data.local.TaskPickEventEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WeightedTaskPickerTest {
    private val now = 1_800_000_000_000L
    private val picker = WeightedTaskPicker(random = Random(7))

    @Test
    fun picker_excludes_archived_tasks() {
        val active = task(id = "active")
        val archived = task(id = "archived", archivedAt = now - 1)

        assertEquals(listOf(active), picker.eligibleTasks(listOf(active, archived), now))
    }

    @Test
    fun picker_excludes_snoozed_tasks_until_snooze_expires() {
        val ready = task(id = "ready", snoozedUntil = now)
        val snoozed = task(id = "snoozed", snoozedUntil = now + 60_000)

        assertEquals(listOf(ready), picker.eligibleTasks(listOf(ready, snoozed), now))
    }

    @Test
    fun never_completed_tasks_get_boosted_above_completed_tasks() {
        val completed = task(id = "done", lastCompletedAt = now - 86_400_000, totalCompletionCount = 1)
        val neverCompleted = task(id = "fresh")

        val completedWeight = picker.weightFor(completed, completions = emptyList(), sessionSkipCounts = emptyMap(), now = now)
        val neverCompletedWeight = picker.weightFor(neverCompleted, completions = emptyList(), sessionSkipCounts = emptyMap(), now = now)

        assertTrue(neverCompletedWeight > completedWeight)
    }

    @Test
    fun starred_tasks_get_boosted_above_unstarred_tasks() {
        val plain = task(id = "plain", lastCompletedAt = now - 86_400_000, totalCompletionCount = 1)
        val starred = task(id = "starred", isStarred = true, lastCompletedAt = now - 86_400_000, totalCompletionCount = 1)

        assertTrue(
            picker.weightFor(starred, emptyList(), emptyMap(), now) >
                picker.weightFor(plain, emptyList(), emptyMap(), now)
        )
    }

    @Test
    fun skipped_tasks_get_penalized_for_the_current_session() {
        val candidate = task(id = "candidate")

        val normalWeight = picker.weightFor(candidate, emptyList(), emptyMap(), now)
        val skippedWeight = picker.weightFor(candidate, emptyList(), mapOf(candidate.id to 2), now)

        assertTrue(skippedWeight < normalWeight)
    }

    @Test
    fun picker_avoids_immediate_repeat_when_another_task_is_available() {
        val previous = task(id = "previous")
        val alternate = task(id = "alternate")
        val history = listOf(pick(taskId = previous.id, pickedAt = now - 1))

        val picked = picker.pick(
            tasks = listOf(previous, alternate),
            completions = emptyList(),
            pickHistory = history,
            sessionSkipCounts = emptyMap(),
            now = now,
        )

        assertEquals(alternate, picked?.task)
    }

    private fun task(
        id: String,
        isStarred: Boolean = false,
        archivedAt: Long? = null,
        snoozedUntil: Long? = null,
        lastCompletedAt: Long? = null,
        totalCompletionCount: Int = 0,
    ) = TaskEntity(
        id = id,
        title = id,
        categoryId = "quick",
        estimateMinutes = 5,
        isStarred = isStarred,
        recurrence = Recurrence.NONE.name,
        createdAt = now - 10_000,
        updatedAt = now - 10_000,
        archivedAt = archivedAt,
        lastCompletedAt = lastCompletedAt,
        lastPickedAt = null,
        snoozedUntil = snoozedUntil,
        totalCompletionCount = totalCompletionCount,
        totalSkipCount = 0,
    )

    @Suppress("unused")
    private fun completion(taskId: String, localDate: String = "2027-01-15") = CompletionEventEntity(
        id = "completion-$taskId",
        taskId = taskId,
        completedAt = now - 5_000,
        localDate = localDate,
    )

    private fun pick(taskId: String, pickedAt: Long) = TaskPickEventEntity(
        id = "pick-$taskId",
        taskId = taskId,
        pickedAt = pickedAt,
        action = TaskPickOutcome.PICKED.storageValue,
    )
}
