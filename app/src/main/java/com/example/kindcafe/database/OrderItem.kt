package com.example.kindcafe.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OrderItem(
    @PrimaryKey(autoGenerate = false)
    val id: String = "0",
    val name: String? = null,
    val add1: Boolean? = null,
    val add2: Boolean? = null,
    val add3: Boolean? = null,
    val size: String? = null,
    val count: String? = null
)
