package com.batb4016.tinynext

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.batb4016.tinynext.domain.TaskPickOutcome
import com.batb4016.tinynext.ui.components.BannerAdView
import com.batb4016.tinynext.ui.model.TaskUiModel
import com.batb4016.tinynext.ui.screens.AddTaskScreen
import com.batb4016.tinynext.ui.screens.HomeScreen
import com.batb4016.tinynext.ui.screens.OnboardingScreen
import com.batb4016.tinynext.ui.screens.PremiumScreen
import com.batb4016.tinynext.ui.screens.ResultScreen
import com.batb4016.tinynext.ui.screens.SettingsPrivacyScreen
import com.batb4016.tinynext.ui.screens.StatsScreen
import com.batb4016.tinynext.ui.screens.TaskListScreen
import com.batb4016.tinynext.ui.theme.TinyNextTheme

@Composable
fun TinyNextApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val application = context.applicationContext as TinyNextApplication
    val viewModel: TinyNextViewModel = viewModel(factory = TinyNextViewModel.Factory(application.container))
    val state by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val startDestination = if (state.settings.onboardingCompleted) Routes.Home else Routes.Onboarding
    val categories = viewModel.mappedCategories()
    val taskModels = viewModel.mappedTasks()
    val currentTaskModel = state.currentTask?.let { current ->
        taskModels.firstOrNull { it.id == current.id } ?: TaskUiModel(
            id = current.id,
            title = current.title,
            categoryName = categories.firstOrNull { it.id == current.categoryId }?.name ?: "Quick",
            estimateMinutes = current.estimateMinutes,
            isStarred = current.isStarred,
            recurrence = current.recurrence,
        )
    }

    TinyNextTheme {
        Scaffold(modifier = modifier.fillMaxSize()) { padding ->
            NavHost(navController = navController, startDestination = startDestination) {
                composable(Routes.Onboarding) {
                    OnboardingScreen(
                        onAddFirstTask = {
                            viewModel.completeOnboarding()
                            navController.navigate(Routes.AddTask) { popUpTo(Routes.Onboarding) { inclusive = true } }
                        },
                        onUseSamples = {
                            viewModel.addSampleTasks()
                            navController.navigate(Routes.Home) { popUpTo(Routes.Onboarding) { inclusive = true } }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable(Routes.Home) {
                    HomeScreen(
                        activeTaskCount = state.tasks.size,
                        completedToday = viewModel.mappedStats().completedToday,
                        showAds = !state.premiumState.isPremium && state.settings.showAds,
                        onPick = {
                            viewModel.pickNext(TaskPickOutcome.PICKED)
                            navController.navigate(Routes.Result)
                        },
                        onAddTask = { navController.navigate(Routes.AddTask) },
                        onViewList = { navController.navigate(Routes.TaskList) },
                        onStats = { navController.navigate(Routes.Stats) },
                        onPremium = { navController.navigate(Routes.Premium) },
                        onSettings = { navController.navigate(Routes.Settings) },
                        adContent = { BannerAdView(isPremium = state.premiumState.isPremium) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable(Routes.AddTask) {
                    AddTaskScreen(
                        categories = categories,
                        onSave = { title: String, categoryId: String, estimateMinutes: Int, starred: Boolean, recurrence: String ->
                            viewModel.addTask(title, categoryId, estimateMinutes, starred, recurrence)
                            navController.navigate(Routes.Home) { popUpTo(Routes.Home) { inclusive = false } }
                        },
                        onCancel = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable(Routes.TaskList) {
                    TaskListScreen(
                        tasks = taskModels,
                        categories = categories,
                        onAdd = { navController.navigate(Routes.AddTask) },
                        onEdit = { taskId -> navController.navigate("${Routes.EditTask}/$taskId") },
                        onArchive = viewModel::archive,
                        onDelete = viewModel::delete,
                        onUndoDelete = viewModel::undoLastDelete,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable(
                    route = "${Routes.EditTask}/{taskId}",
                    arguments = listOf(navArgument("taskId") { nullable = false })
                ) { entry ->
                    val taskId = entry.arguments?.getString("taskId").orEmpty()
                    val task = state.tasks.firstOrNull { it.id == taskId }
                    if (task == null) {
                        navController.popBackStack()
                    } else {
                        AddTaskScreen(
                            categories = categories,
                            initialTitle = task.title,
                            initialCategoryId = task.categoryId,
                            initialEstimateMinutes = task.estimateMinutes,
                            initialStarred = task.isStarred,
                            initialRecurrence = task.recurrence,
                            onSave = { title: String, categoryId: String, estimateMinutes: Int, starred: Boolean, recurrence: String ->
                                viewModel.updateTask(task.id, title, categoryId, estimateMinutes, starred, recurrence)
                                navController.popBackStack()
                            },
                            onCancel = { navController.popBackStack() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                composable(Routes.Result) {
                    ResultScreen(
                        task = currentTaskModel,
                        onDone = {
                            viewModel.doneCurrent()
                            navController.navigate(Routes.Stats) {
                                popUpTo(Routes.Home) { inclusive = false }
                            }
                        },
                        onSnooze = {
                            viewModel.snoozeCurrent()
                            navController.navigate(Routes.Home)
                        },
                        onSkip = { viewModel.skipCurrent() },
                        onAnother = { viewModel.another() },
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable(Routes.Stats) {
                    StatsScreen(
                        stats = viewModel.mappedStats(),
                        onPickNext = {
                            viewModel.pickNext(TaskPickOutcome.PICKED)
                            navController.navigate(Routes.Result)
                        },
                        onBack = {
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Home) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable(Routes.Premium) {
                    PremiumScreen(
                        premiumState = state.premiumState,
                        onBuyPremium = {
                            (context as? Activity)?.let(viewModel::buyPremium)
                        },
                        onRestorePurchase = viewModel::restorePurchase,
                        onBack = { navController.popBackStack() },
                        debugPremiumOverrideEnabled = state.premiumState.isPremium,
                        onDebugPremiumOverrideChange = viewModel::setDebugPremiumOverride,
                    )
                }
                composable(Routes.Settings) {
                    SettingsPrivacyScreen(
                        isPremium = state.premiumState.isPremium,
                        appVersion = BuildConfig.VERSION_NAME,
                        onRestorePurchase = viewModel::restorePurchase,
                        onDeleteAllLocalData = viewModel::deleteAllLocalData,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

private object Routes {
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val AddTask = "add_task"
    const val TaskList = "task_list"
    const val EditTask = "edit_task"
    const val Result = "result"
    const val Stats = "stats"
    const val Premium = "premium"
    const val Settings = "settings"
}
