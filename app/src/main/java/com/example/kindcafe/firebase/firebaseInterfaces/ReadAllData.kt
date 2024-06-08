package com.example.kindcafe.firebase.firebaseInterfaces

import com.example.kindcafe.database.Dish

interface ReadAllData {
    fun readAll(data: List<Dish>)
}