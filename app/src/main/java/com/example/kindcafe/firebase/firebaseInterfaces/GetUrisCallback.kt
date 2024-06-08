package com.example.kindcafe.firebase.firebaseInterfaces

import com.example.kindcafe.database.Dish

interface GetUrisCallback {
    fun getUris(newData: List<Dish>)
}