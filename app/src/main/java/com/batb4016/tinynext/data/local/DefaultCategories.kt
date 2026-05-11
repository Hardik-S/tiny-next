package com.batb4016.tinynext.data.local

object DefaultCategories {
    private const val SEEDED_AT = 0L

    val values = listOf(
        CategoryEntity(id = "quick", name = "Quick", isDefault = true, createdAt = SEEDED_AT, sortOrder = 0),
        CategoryEntity(id = "home", name = "Home", isDefault = true, createdAt = SEEDED_AT, sortOrder = 1),
        CategoryEntity(id = "work", name = "Work", isDefault = true, createdAt = SEEDED_AT, sortOrder = 2),
        CategoryEntity(id = "study", name = "Study", isDefault = true, createdAt = SEEDED_AT, sortOrder = 3),
        CategoryEntity(id = "errand", name = "Errand", isDefault = true, createdAt = SEEDED_AT, sortOrder = 4),
        CategoryEntity(id = "health", name = "Health", isDefault = true, createdAt = SEEDED_AT, sortOrder = 5),
    )
}
