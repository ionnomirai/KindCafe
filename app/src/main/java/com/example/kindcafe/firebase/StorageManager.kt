package com.example.kindcafe.firebase

import android.util.Log
import com.example.kindcafe.database.Dish
import com.example.kindcafe.firebase.firebaseInterfaces.GetUrisCallback
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

class StorageManager {
    private val my_tag = "StorageManagerTag"
    private var storageReference = FirebaseStorage.getInstance()

    suspend fun readUri(data: List<Dish>, getUrisCallback: GetUrisCallback){
        val flow1 = MutableStateFlow(0)
        val uriDishList: MutableList<Dish> = mutableListOf()
        Log.d(my_tag, "size: ${data.size}")
        data.forEach {dish ->
            storageReference
                .getReference("dish_images/${dish.name}_${dish.id}.png")
                .downloadUrl
                .addOnSuccessListener {uri ->
                    uriDishList.add(dish.copy(uri = uri.toString()))
                    Log.d(my_tag, "in success: ${uri.toString()}")
                    flow1.value += 1
                }
        }
        flow1.collect{
            if(it == data.size-1){
                Log.d(my_tag, "after all")
                getUrisCallback.getUris(uriDishList)
            }
        }
    }
}