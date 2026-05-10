package com.batb4016.tinynext.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TaskEntity::class,
        CategoryEntity::class,
        CompletionEventEntity::class,
        TaskPickEventEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class TinyNextDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun completionEventDao(): CompletionEventDao
    abstract fun taskPickEventDao(): TaskPickEventDao

    companion object {
        const val DATABASE_NAME = "tiny_next.db"

        @Volatile
        private var instance: TinyNextDatabase? = null

        fun getInstance(context: Context): TinyNextDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }

        private fun buildDatabase(context: Context): TinyNextDatabase =
            Room.databaseBuilder(context, TinyNextDatabase::class.java, DATABASE_NAME)
                .addCallback(DefaultCategoryCallback())
                .build()
    }
}

private class DefaultCategoryCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        TinyNextDatabaseSeed.insertDefaultCategories(db)
    }
}
