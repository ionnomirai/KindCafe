package com.example.kindcafe.firebase.firebaseInterfaces

import com.example.kindcafe.data.Dish

interface ReadAndSplitCategories {
    fun readAndSplit(data: Map<Int, List<Dish>>)
}