package com.example.kindcafe.adapters.callbacks

import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.Favorites

interface ItemMoveDirections {
    fun detailed(dish: Dish)
    fun putToBag(dish: Dish)

    fun putToFavorite(favoriteDish: Favorites)

    fun delFromFavorite(favoriteDish: Favorites)

    fun checkFavorites(favoriteDish: Favorites): Boolean

    fun checkUserExist(): Boolean
}