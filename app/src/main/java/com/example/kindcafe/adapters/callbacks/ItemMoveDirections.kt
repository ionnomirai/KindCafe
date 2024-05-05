package com.example.kindcafe.adapters.callbacks

import android.content.res.ColorStateList
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.Favorites

interface ItemMoveDirections {
    fun detailed(dish: Dish)
    fun putToBag(dish: Dish)

    fun delFromBag(dish: Dish)

    fun checkBag(dish: Dish): Boolean

    fun putToFavorite(favoriteDish: Favorites)

    fun delFromFavorite(favoriteDish: Favorites)

    fun checkFavorites(favoriteDish: Favorites): Boolean

    fun checkUserExist(): Boolean

    fun getTint(isPress: Boolean): ColorStateList?
}