package com.example.kindcafe.data

data class DetailedOrderItem(
    val id: String = "0",
    val name: String? = null,
    val price: String? = null,
    val description: String? = null,
    val category: String? = null,
    val characteristic: String? = null,
    val uriSmall: String? = null,
    val uriBig: String? = null,
    val add1: Boolean? = null,
    val add2: Boolean? = null,
    val add3: Boolean? = null,
    val size: String? = null,
    val count: String? = null
)
