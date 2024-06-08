package com.example.kindcafe.firebase.firebaseInterfaces

import com.example.kindcafe.database.Dish

interface ReadAndSplitCategories {
    //fun readAndSplit(data: Map<Int, List<Dish>>)
    fun readAndSplit(data: List<Dish>)
}