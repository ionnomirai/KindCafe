package com.example.kindcafe.data

import com.example.kindcafe.database.Favorites
import com.example.kindcafe.database.OrderItem
import com.example.kindcafe.database.UserPersonal

data class AllUserData(
    var favorites: MutableList<Favorites>? = null,
    var order: MutableList<OrderItem>? = null,
    var personal: UserPersonal? = null
)
