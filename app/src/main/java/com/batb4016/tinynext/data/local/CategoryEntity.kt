package com.batb4016.tinynext.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val isDefault: Boolean,
    val createdAt: Long,
    val sortOrder: Int,
)
