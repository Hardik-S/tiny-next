package com.batb4016.tinynext.domain

import com.batb4016.tinynext.data.local.CompletionEventEntity
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class TaskStats(
    val completedToday: Int,
    val totalCompleted: Int,
    val currentStreakDays: Int,
    val bestStreakDays: Int,
    val recentCompletions: List<CompletionEventEntity>,
)

class StatsCalculator(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = clock.zone,
) {
    fun calculate(
        completions: List<CompletionEventEntity>,
        recentLimit: Int = DEFAULT_RECENT_LIMIT,
    ): TaskStats {
        require(recentLimit >= 0) { "Recent completion limit cannot be negative." }
        val today = LocalDate.now(clock)
        val completionDates = completions
            .map { it.completedAt.toLocalDate() }
            .toSet()

        return TaskStats(
            completedToday = completions.count { it.localDate == today.toString() || it.completedAt.toLocalDate() == today },
            totalCompleted = completions.size,
            currentStreakDays = currentStreak(today, completionDates),
            bestStreakDays = bestStreak(completionDates),
            recentCompletions = completions
                .sortedByDescending { it.completedAt }
                .take(recentLimit),
        )
    }

    private fun currentStreak(today: LocalDate, completionDates: Set<LocalDate>): Int {
        if (completionDates.isEmpty()) return 0

        // A streak remains current if the user has completed something today or yesterday.
        val startingDate = when {
            today in completionDates -> today
            today.minusDays(1) in completionDates -> today.minusDays(1)
            else -> return 0
        }

        var cursor = startingDate
        var count = 0
        while (cursor in completionDates) {
            count += 1
            cursor = cursor.minusDays(1)
        }
        return count
    }

    private fun bestStreak(completionDates: Set<LocalDate>): Int {
        if (completionDates.isEmpty()) return 0
        val sortedDates = completionDates.sorted()
        var best = 1
        var current = 1

        for (index in 1 until sortedDates.size) {
            current = if (sortedDates[index - 1].plusDays(1) == sortedDates[index]) {
                current + 1
            } else {
                1
            }
            best = maxOf(best, current)
        }

        return best
    }

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

    companion object {
        const val DEFAULT_RECENT_LIMIT = 10
    }
}
