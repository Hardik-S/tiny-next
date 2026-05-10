package com.batb4016.tinynext.data.settings

data class UserSettings(
    val onboardingCompleted: Boolean = false,
    val selectedTheme: String = "SYSTEM",
    val freeCustomCategoryLimit: Int = 3,
    val advancedWeightingEnabled: Boolean = false,
    val showAds: Boolean = true,
    val lastOpenedLocalDate: String? = null,
)
