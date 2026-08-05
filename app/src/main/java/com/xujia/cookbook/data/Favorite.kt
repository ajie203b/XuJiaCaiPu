package com.xujia.cookbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 收藏菜品实体
 */
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val dishId: String,
    val dishName: String,
    val category: String,
    val mainImage: String,
    val difficulty: Int,
    val timestamp: Long = System.currentTimeMillis()
)
