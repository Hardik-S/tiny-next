package com.batb4016.tinynext.domain

import com.batb4016.tinynext.data.local.CompletionEventEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class StatsCalculatorTest {
    private val zone = ZoneId.of("America/Toronto")
    private val clock = Clock.fixed(Instant.parse("2026-05-10T16:00:00Z"), zone)
    private val calculator = StatsCalculator(clock = clock, zoneId = zone)

    @Test
    fun stats_calculate_completed_today_and_total_completed() {
        val events = listOf(
            completionAt("2026-05-10T09:15:00-04:00"),
            completionAt("2026-05-10T11:30:00-04:00"),
            completionAt("2026-05-09T17:45:00-04:00"),
        )

        val stats = calculator.calculate(events, recentLimit = 5)

        assertEquals(2, stats.completedToday)
        assertEquals(3, stats.totalCompleted)
        assertEquals(events.sortedByDescending { it.completedAt }, stats.recentCompletions)
    }

    @Test
    fun streak_calculation_uses_distinct_completion_dates() {
        val events = listOf(
            completionAt("2026-05-10T09:00:00-04:00"),
            completionAt("2026-05-09T10:00:00-04:00"),
            completionAt("2026-05-08T10:00:00-04:00"),
            completionAt("2026-05-06T10:00:00-04:00"),
            completionAt("2026-05-05T10:00:00-04:00"),
        )

        val stats = calculator.calculate(events, recentLimit = 3)

        assertEquals(3, stats.currentStreakDays)
        assertEquals(3, stats.bestStreakDays)
        assertEquals(3, stats.recentCompletions.size)
    }

    private fun completionAt(isoDateTime: String): CompletionEventEntity {
        val completedAt = ZonedDateTime.parse(isoDateTime).toInstant().toEpochMilli()
        val localDate = ZonedDateTime.parse(isoDateTime).toLocalDate().toString()
        return CompletionEventEntity(
            id = "completion-$completedAt",
            taskId = "task-$completedAt",
            completedAt = completedAt,
            localDate = localDate,
        )
    }
}
