package com.example.kindcafe.adapters.callbacks

import com.example.kindcafe.database.Dish

interface ItemMoveDirections {
    fun detailed(dish: Dish)
    fun putToBag(dish: Dish)
}