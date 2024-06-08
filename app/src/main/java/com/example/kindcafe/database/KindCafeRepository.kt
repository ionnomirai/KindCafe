package com.example.kindcafe.database

import android.content.Context
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Room
import androidx.room.Upsert
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
    /*------------------------------------Dish------------------------------------*/
    suspend fun insertDish(dish: List<Dish>) = database.kindCafeDao().insertDish(dish)

    fun getAllDishes(): Flow<List<Dish>> = database.kindCafeDao().getAllDishes()

    fun getDishByCategory(category: Categories): Flow<List<Dish>> =
        database.kindCafeDao().getDishesByCategory(category.categoryName)

    fun getDish(id: String, name: String): Flow<Dish> =
        database.kindCafeDao().getDish(id, name)

    suspend fun getDish1(id: String, name: String): Dish =
        database.kindCafeDao().getDishSimple(id, name)


    /*------------------------------------Favorite------------------------------------*/

    suspend fun insertFavorite(favorite: Favorites) = database.kindCafeDao().insertFavorites(favorite)

    fun getFavorites(): Flow<List<Favorites>> = database.kindCafeDao().getAllFavorites()

    suspend fun deleteFavDish(fav: Favorites) = database.kindCafeDao().deleteFavDish(fav)

    suspend fun deleteAllFav() = database.kindCafeDao().deleteAllFav()

    /*------------------------------------Personal------------------------------------*/

    suspend fun getPersonalDataLocal() = database.kindCafeDao().getPersonalLocal()

    suspend fun setPersonalDataLocal(personal: UserPersonal) = database.kindCafeDao().setPersonalLocal(personal)

    suspend fun deletePersonalDataLocal(personal: UserPersonal) = database.kindCafeDao().deletePersonalLocal(personal)

    suspend fun deleteAllPersonal() = database.kindCafeDao().deleteAllPersonal()

    /*------------------------------------To Basket------------------------------------*/

    fun getOrderItemsLocal(): Flow<List<OrderItem>> = database.kindCafeDao().getOrderItemsLocal()

    suspend fun setOrderItemsLocal(order: OrderItem) = database.kindCafeDao().setOrderItemsLocal(order)

    suspend fun deleteOrderItemsLocal(order: OrderItem) = database.kindCafeDao().deleteOrderItemsLocal(order)

    suspend fun deleteAllOrderItemsLocal() = database.kindCafeDao().deleteAllOrderItemsLocal()

    /*------------------------------------OrderItemPlaced - to server------------------------------------*/

    fun getOrderPlacedLocal(): Flow<List<OrderItemPlaced>> = database.kindCafeDao().getOrderPlacedLocal()

    suspend fun setOrderPlacedLocal(orderP: OrderItemPlaced) = database.kindCafeDao().setOrderPlacedLocal(orderP)

    suspend fun deleteAllOrderPlacedLocal() = database.kindCafeDao().deleteAllOrderPlacedLocal()

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