package com.xujia.cookbook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE dishId = :dishId)")
    fun isFavorite(dishId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE dishId = :dishId")
    suspend fun deleteById(dishId: String)
}
