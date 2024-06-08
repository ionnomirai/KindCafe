package com.example.kindcafe.utils

import com.example.kindcafe.data.Categories

enum class Locations(val nameL: String) {
    HOME("All dishes"),
    FAVORITES("Favorites"),
    BASKET("Basket"),
    SHOW_SPARKLING_DRINKS(Categories.SparklingDrinks.categoryName),
    SHOW_NON_SPARKLING_DRINKS(Categories.NonSparklingDrinks.categoryName),
    SHOW_SWEETS(Categories.Sweets.categoryName),
    SHOW_CAKES(Categories.Cakes.categoryName)
}