package com.example.kindcafe.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface KindCafeDao {
    @Upsert
    suspend fun insertDish(dish: List<Dish>)

    @Query("SELECT * FROM dish WHERE category = :categoryCur")
    fun getDishesByCategory(categoryCur: String): Flow<List<Dish>>
}