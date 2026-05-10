package com.batb4016.tinynext

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batb4016.tinynext.data.TinyNextRepository
import com.batb4016.tinynext.data.local.CategoryEntity
import com.batb4016.tinynext.data.local.CompletionEventEntity
import com.batb4016.tinynext.data.local.TaskEntity
import com.batb4016.tinynext.data.local.TaskPickEventEntity
import com.batb4016.tinynext.data.monetization.BillingRepository
import com.batb4016.tinynext.data.monetization.PremiumState
import com.batb4016.tinynext.data.settings.SettingsRepository
import com.batb4016.tinynext.data.settings.UserSettings
import com.batb4016.tinynext.domain.StatsCalculator
import com.batb4016.tinynext.domain.TaskPickOutcome
import com.batb4016.tinynext.domain.WeightedTaskPicker
import com.batb4016.tinynext.ui.model.CategoryUiModel
import com.batb4016.tinynext.ui.model.StatsUiModel
import com.batb4016.tinynext.ui.model.TaskUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TinyNextUiState(
    val settings: UserSettings = UserSettings(),
    val premiumState: PremiumState = PremiumState.Free,
    val tasks: List<TaskEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val completions: List<CompletionEventEntity> = emptyList(),
    val pickEvents: List<TaskPickEventEntity> = emptyList(),
    val currentTask: TaskEntity? = null,
    val sessionSkipCounts: Map<String, Int> = emptyMap(),
)

class TinyNextViewModel(
    private val repository: TinyNextRepository,
    private val settingsRepository: SettingsRepository,
    private val billingRepository: BillingRepository,
) : ViewModel() {
    private val picker = WeightedTaskPicker()
    private val statsCalculator = StatsCalculator()
    private val currentTask = MutableStateFlow<TaskEntity?>(null)
    private val sessionSkipCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    private var lastDeletedTask: TaskEntity? = null

    val uiState: StateFlow<TinyNextUiState> = combine(
        settingsRepository.settings,
        billingRepository.premiumState,
        repository.tasks,
        repository.categories,
        repository.completions,
        repository.pickEvents,
        currentTask,
        sessionSkipCounts,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        TinyNextUiState(
            settings = values[0] as UserSettings,
            premiumState = values[1] as PremiumState,
            tasks = values[2] as List<TaskEntity>,
            categories = values[3] as List<CategoryEntity>,
            completions = values[4] as List<CompletionEventEntity>,
            pickEvents = values[5] as List<TaskPickEventEntity>,
            currentTask = values[6] as TaskEntity?,
            sessionSkipCounts = values[7] as Map<String, Int>,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TinyNextUiState())

    init {
        viewModelScope.launch {
            repository.ensureDefaultCategories()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsRepository.setOnboardingCompleted(true) }
    }

    fun addSampleTasks() {
        viewModelScope.launch {
            repository.addSampleTasks()
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    fun addTask(title: String, categoryId: String, estimateMinutes: Int, starred: Boolean, recurrence: String) {
        viewModelScope.launch {
            repository.addTask(title, categoryId, estimateMinutes, starred, recurrence)
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    fun updateTask(id: String, title: String, categoryId: String, estimateMinutes: Int, starred: Boolean, recurrence: String) {
        viewModelScope.launch { repository.updateTask(id, title, categoryId, estimateMinutes, starred, recurrence) }
    }

    fun pickNext(action: TaskPickOutcome = TaskPickOutcome.PICKED) {
        val state = uiState.value
        val picked = picker.pick(
            tasks = state.tasks,
            completions = state.completions,
            pickHistory = state.pickEvents,
            sessionSkipCounts = state.sessionSkipCounts,
            now = System.currentTimeMillis(),
        )?.task
        currentTask.value = picked
        if (picked != null) {
            viewModelScope.launch { repository.markPicked(picked.id, action) }
        }
    }

    fun doneCurrent() {
        val task = currentTask.value ?: return
        viewModelScope.launch {
            repository.markDone(task.id)
            currentTask.value = null
        }
    }

    fun skipCurrent() {
        val task = currentTask.value ?: return
        sessionSkipCounts.value = sessionSkipCounts.value + (task.id to ((sessionSkipCounts.value[task.id] ?: 0) + 1))
        viewModelScope.launch {
            repository.skip(task.id)
            pickNext(TaskPickOutcome.SKIPPED)
        }
    }

    fun snoozeCurrent() {
        val task = currentTask.value ?: return
        viewModelScope.launch {
            repository.snooze(task.id)
            currentTask.value = null
        }
    }

    fun another() {
        val task = currentTask.value ?: return
        sessionSkipCounts.value = sessionSkipCounts.value + (task.id to ((sessionSkipCounts.value[task.id] ?: 0) + 1))
        viewModelScope.launch {
            repository.markPicked(task.id, TaskPickOutcome.ANOTHER)
            pickNext(TaskPickOutcome.ANOTHER)
        }
    }

    fun archive(taskId: String) {
        viewModelScope.launch { repository.archive(taskId) }
    }

    fun delete(taskId: String) {
        val task = uiState.value.tasks.firstOrNull { it.id == taskId } ?: return
        lastDeletedTask = task
        viewModelScope.launch { repository.delete(task) }
    }

    fun undoLastDelete() {
        val task = lastDeletedTask ?: return
        viewModelScope.launch {
            repository.restoreTask(task)
            lastDeletedTask = null
        }
    }

    fun restorePurchase() {
        billingRepository.restorePurchases()
    }

    fun buyPremium(activity: Activity) {
        billingRepository.launchRemoveAdsPurchase(activity)
    }

    fun setDebugPremiumOverride(enabled: Boolean) {
        billingRepository.setDebugPremiumOverride(enabled)
    }

    fun deleteAllLocalData() {
        viewModelScope.launch { repository.deleteAllLocalData() }
    }

    fun mappedTasks(): List<TaskUiModel> {
        val categoriesById = uiState.value.categories.associateBy { it.id }
        return uiState.value.tasks.map { task ->
            TaskUiModel(
                id = task.id,
                title = task.title,
                categoryName = categoriesById[task.categoryId]?.name ?: "Quick",
                estimateMinutes = task.estimateMinutes,
                isStarred = task.isStarred,
                recurrence = task.recurrence,
                lastDoneLabel = if (task.lastCompletedAt == null) "Not done yet" else "Done before",
            )
        }
    }

    fun mappedCategories(): List<CategoryUiModel> =
        uiState.value.categories.map { CategoryUiModel(id = it.id, name = it.name) }

    fun mappedStats(): StatsUiModel {
        val stats = statsCalculator.calculate(uiState.value.completions)
        return StatsUiModel(
            completedToday = stats.completedToday,
            totalCompleted = stats.totalCompleted,
            currentStreak = stats.currentStreakDays,
            bestStreak = stats.bestStreakDays,
            recentCompletions = stats.recentCompletions.map { it.localDate },
        )
    }

    override fun onCleared() {
        billingRepository.close()
        super.onCleared()
    }

    class Factory(
        private val container: TinyNextContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TinyNextViewModel(container.repository, container.settingsRepository, container.billingRepository) as T
    }
}
