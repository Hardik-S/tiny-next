package com.batb4016.tinynext.domain

import com.batb4016.tinynext.data.local.CompletionEventEntity
import com.batb4016.tinynext.data.local.TaskEntity
import com.batb4016.tinynext.data.local.TaskPickEventEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.random.Random

data class PickedTask(
    val task: TaskEntity,
    val weight: Int,
)

/**
 * Encodes the product promise: when the user asks for help, return one concrete
 * task that is available now and biased toward small, fresh, unfinished work.
 * The arithmetic follows the launch spec directly so Play-facing behavior is
 * easy to audit later.
 */
class WeightedTaskPicker(
    private val random: Random = Random.Default,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun eligibleTasks(tasks: List<TaskEntity>, now: Long): List<TaskEntity> =
        tasks.filter { task ->
            task.archivedAt == null &&
                (task.snoozedUntil == null || task.snoozedUntil <= now) &&
                recurrenceAllowsToday(task, now)
        }

    fun weightFor(
        task: TaskEntity,
        completions: List<CompletionEventEntity>,
        sessionSkipCounts: Map<String, Int>,
        now: Long,
    ): Int {
        val today = now.toLocalDate()
        val lastCompleted = task.lastCompletedAt
        val completedToday = lastCompleted?.toLocalDate() == today ||
            completions.any { it.taskId == task.id && it.localDate == today.toString() }

        var weight = 10
        if (lastCompleted == null && task.totalCompletionCount == 0) weight += 12
        if (lastCompleted != null) {
            val daysSinceLastDone = ChronoUnit.DAYS.between(lastCompleted.toLocalDate(), today).toInt().coerceAtLeast(0)
            weight += minOf(daysSinceLastDone * 2, 20)
        }
        if (task.isStarred) weight += 8
        if (task.estimateMinutes <= 5) weight += 5
        if (task.categoryId == "quick") weight += 3
        if (now - task.createdAt <= ONE_DAY_MILLIS) weight += 4
        if (task.recurrence == Recurrence.DAILY.name && !completedToday) weight += 5
        if (task.recurrence == Recurrence.WEEKLY.name && !completedThisWeek(task, completions, now)) weight += 3

        weight -= (sessionSkipCounts[task.id] ?: 0) * 5
        weight -= minOf(task.totalSkipCount, 10)
        if (task.estimateMinutes >= 30) weight -= 4
        if (task.lastPickedAt != null && now - task.lastPickedAt < TEN_MINUTES_MILLIS) weight -= 8

        return weight.coerceIn(1, 50)
    }

    fun pick(
        tasks: List<TaskEntity>,
        completions: List<CompletionEventEntity>,
        pickHistory: List<TaskPickEventEntity>,
        sessionSkipCounts: Map<String, Int>,
        now: Long,
    ): PickedTask? {
        val eligible = eligibleTasks(tasks, now)
        if (eligible.isEmpty()) return null

        val lastPickedTaskId = pickHistory
            .filter { it.action == TaskPickOutcome.PICKED.storageValue || it.action == TaskPickOutcome.ANOTHER.storageValue }
            .maxByOrNull { it.pickedAt }
            ?.taskId

        val pool = if (lastPickedTaskId != null && eligible.size > 1) {
            eligible.filterNot { it.id == lastPickedTaskId }.ifEmpty { eligible }
        } else {
            eligible
        }

        val weighted = pool.map { task ->
            PickedTask(task, weightFor(task, completions, sessionSkipCounts, now))
        }
        val totalWeight = weighted.sumOf { it.weight }
        var threshold = random.nextInt(totalWeight)
        for (candidate in weighted) {
            threshold -= candidate.weight
            if (threshold < 0) return candidate
        }
        return weighted.last()
    }

    private fun recurrenceAllowsToday(task: TaskEntity, now: Long): Boolean =
        when (task.recurrence) {
            Recurrence.NONE.name, "" -> true
            Recurrence.DAILY.name -> task.lastCompletedAt?.toLocalDate() != now.toLocalDate()
            Recurrence.WEEKLY.name -> !sameWeek(task.lastCompletedAt, now)
            else -> true
        }

    private fun completedThisWeek(
        task: TaskEntity,
        completions: List<CompletionEventEntity>,
        now: Long,
    ): Boolean = sameWeek(task.lastCompletedAt, now) || completions
        .filter { it.taskId == task.id }
        .any { sameWeek(it.completedAt, now) }

    private fun sameWeek(first: Long?, second: Long): Boolean {
        if (first == null) return false
        val a = first.toLocalDate()
        val b = second.toLocalDate()
        return a.year == b.year && a.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR) ==
            b.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    }

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

    companion object {
        private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L
        private const val TEN_MINUTES_MILLIS = 10L * 60L * 1000L
    }
}

enum class Recurrence {
    NONE,
    DAILY,
    WEEKLY
}
