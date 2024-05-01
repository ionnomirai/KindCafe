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

    fun getSparklingDrinks(): Flow<List<Dish>> =
        database.kindCafeDao().getDishesByCategory(Categories.SparklingDrinks.categoryName)

    fun getNonSparklingDrinks(): Flow<List<Dish>> =
        database.kindCafeDao().getDishesByCategory(Categories.NonSparklingDrinks.categoryName)

    /* ... continue funs*/

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