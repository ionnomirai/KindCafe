package com.example.kindcafe.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Favorites(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    val dishId: String? = null,
    val dishName: String? = null
)
