package com.batb4016.tinynext.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setSelectedTheme(theme: String)
    suspend fun setAdvancedWeightingEnabled(enabled: Boolean)
    suspend fun setShowAds(showAds: Boolean)
    suspend fun setLastOpenedLocalDate(localDate: String)
}
