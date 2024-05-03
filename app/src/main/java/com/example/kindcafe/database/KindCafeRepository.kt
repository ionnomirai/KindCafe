package com.example.kindcafe.database

import android.content.Context
import androidx.room.Room
import com.example.kindcafe.data.Categories
import kotlinx.coroutines.flow.Flow

class KindCafeRepository private constructor(
    context: Context
){
    private val DATABASE_NAME = "kind_cafe_database"
    private val database: KindCafeDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            KindCafeDatabase::class.java,
            DATABASE_NAME
        )
        .build()

    suspend fun insertDish(dish: List<Dish>) = database.kindCafeDao().insertDish(dish)

    fun getAllDishes(): Flow<List<Dish>> = database.kindCafeDao().getAllDishes()

    fun getDishByCategory(category: Categories): Flow<List<Dish>> =
        database.kindCafeDao().getDishesByCategory(category.categoryName)

    fun getDish(id: String, name: String): Flow<Dish> =
        database.kindCafeDao().getDish(id, name)

    companion object{
        private var INSTANCE: KindCafeRepository? = null

        fun initialize(context: Context){
            if(INSTANCE == null){
                INSTANCE = KindCafeRepository(context)
            }
        }

        fun get(): KindCafeRepository{
            return INSTANCE ?: throw IllegalStateException("The local KindCafeDatabase is not initialized.")
        }
    }
}