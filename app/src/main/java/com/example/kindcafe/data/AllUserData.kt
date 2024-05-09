package com.example.kindcafe.data

import com.example.kindcafe.database.Favorites
import com.example.kindcafe.database.OrderItem
import com.example.kindcafe.database.OrderItemPlaced
import com.example.kindcafe.database.UserPersonal

data class AllUserData(
    var favorites: MutableList<Favorites>? = null,
    var orderBasket: MutableList<OrderItem>? = null,
    var orderPlaced: MutableList<OrderItemPlaced>? = null,
    var personal: UserPersonal? = null
)
