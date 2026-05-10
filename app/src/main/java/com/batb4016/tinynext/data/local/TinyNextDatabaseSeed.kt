package com.batb4016.tinynext.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

object TinyNextDatabaseSeed {
    fun insertDefaultCategories(db: SupportSQLiteDatabase) {
        DefaultCategories.values.forEach { category ->
            db.execSQL(
                """
                INSERT OR IGNORE INTO categories(id, name, isDefault, createdAt, sortOrder)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any>(category.id, category.name, if (category.isDefault) 1 else 0, category.createdAt, category.sortOrder),
            )
        }
    }
}
