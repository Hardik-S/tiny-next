package com.batb4016.tinynext.ui.model

/**
 * UI-facing models stay deliberately small. The Room entities include richer
 * persistence fields, while these types describe only what a screen needs to
 * render and report user intent back to the app state holder.
 */
data class TaskUiModel(
    val id: String,
    val title: String,
    val categoryName: String,
    val estimateMinutes: Int,
    val isStarred: Boolean,
    val recurrence: String,
    val lastDoneLabel: String = "Not done yet"
)

data class CategoryUiModel(
    val id: String,
    val name: String
)

data class StatsUiModel(
    val completedToday: Int = 0,
    val totalCompleted: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val recentCompletions: List<String> = emptyList()
)

