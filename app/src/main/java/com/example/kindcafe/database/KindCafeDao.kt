package com.example.kindcafe.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface KindCafeDao {

    /*------------------------------------Dish------------------------------------*/
    @Upsert
    suspend fun insertDish(dish: List<Dish>)

    @Query("Select * FROM dish")
    fun getAllDishes(): Flow<List<Dish>>

    @Query("SELECT * FROM dish WHERE category = :categoryCur")
    fun getDishesByCategory(categoryCur: String): Flow<List<Dish>>

    @Query("SELECT * FROM dish WHERE id = :id AND name = :name")
    fun getDish(id: String, name: String): Flow<Dish>


    /*------------------------------------Favorite------------------------------------*/
    @Upsert
    suspend fun insertFavorites(favorites: Favorites)

    @Query("Select * FROM favorites")
    fun getAllFavorites(): Flow<List<Favorites>>

    @Query("SELECT * FROM dish WHERE id = :id AND name = :name")
    suspend fun getDishSimple(id: String, name: String): Dish
    @Delete
    suspend fun deleteFavDish(fav: Favorites)

    // delete all records
    @Query("DELETE FROM favorites")
    suspend fun deleteAllFav()

    /*------------------------------------Personal------------------------------------*/

    @Query("SELECT * FROM userpersonal")
    fun getPersonalLocal(): Flow<UserPersonal>

    @Upsert
    suspend fun setPersonalLocal(personal: UserPersonal)

    @Delete
    suspend fun deletePersonalLocal(personal: UserPersonal)

    @Query("DELETE FROM userpersonal")
    suspend fun deleteAllPersonal()

    /*------------------------------------Order------------------------------------*/

    @Query("SELECT * FROM orderitem")
    fun getOrderItemsLocal(): Flow<List<OrderItem>>

    @Upsert
    suspend fun setOrderItemsLocal(order: OrderItem)

    @Delete
    suspend fun deleteOrderItemsLocal(order: OrderItem)

    @Query("DELETE FROM orderitem")
    suspend fun deleteAllOrderItemsLocal()
}