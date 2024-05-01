package com.example.kindcafe.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Dish(
    @PrimaryKey(autoGenerate = false)
    val id: String = "0",
    val name: String? = null,
    val price: String? = null,
    val description: String? = null,
    val category: String? = null,
    val characteristic: String? = null,
)
