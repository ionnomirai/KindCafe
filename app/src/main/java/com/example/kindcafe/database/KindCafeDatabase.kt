package com.example.kindcafe.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Dish::class, Favorites::class, UserPersonal::class],
    version = 1
)
abstract class KindCafeDatabase: RoomDatabase() {
    abstract fun kindCafeDao(): KindCafeDao
}