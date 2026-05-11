package com.batb4016.tinynext.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.tinyNextSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tiny_next_settings",
)

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    constructor(context: Context) : this(context.tinyNextSettingsDataStore)

    override val settings: Flow<UserSettings> = dataStore.data.map { preferences ->
        UserSettings(
            onboardingCompleted = preferences[Keys.onboardingCompleted] ?: false,
            selectedTheme = preferences[Keys.selectedTheme] ?: "SYSTEM",
            freeCustomCategoryLimit = preferences[Keys.freeCustomCategoryLimit] ?: 3,
            advancedWeightingEnabled = preferences[Keys.advancedWeightingEnabled] ?: false,
            showAds = preferences[Keys.showAds] ?: true,
            lastOpenedLocalDate = preferences[Keys.lastOpenedLocalDate],
        )
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.onboardingCompleted] = completed }
    }

    override suspend fun setSelectedTheme(theme: String) {
        dataStore.edit { it[Keys.selectedTheme] = theme }
    }

    override suspend fun setAdvancedWeightingEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.advancedWeightingEnabled] = enabled }
    }

    override suspend fun setShowAds(showAds: Boolean) {
        dataStore.edit { it[Keys.showAds] = showAds }
    }

    override suspend fun setLastOpenedLocalDate(localDate: String) {
        dataStore.edit { it[Keys.lastOpenedLocalDate] = localDate }
    }

    private object Keys {
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val selectedTheme = stringPreferencesKey("selected_theme")
        val freeCustomCategoryLimit = intPreferencesKey("free_custom_category_limit")
        val advancedWeightingEnabled = booleanPreferencesKey("advanced_weighting_enabled")
        val showAds = booleanPreferencesKey("show_ads")
        val lastOpenedLocalDate = stringPreferencesKey("last_opened_local_date")
    }
}
