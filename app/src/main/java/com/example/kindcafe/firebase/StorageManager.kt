package com.example.kindcafe.firebase

import android.util.Log
import com.example.kindcafe.database.Dish
import com.example.kindcafe.firebase.firebaseEnums.UriSize
import com.example.kindcafe.firebase.firebaseInterfaces.GetUrisCallback
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

class StorageManager {
    private val my_tag = "StorageManagerTag"
    private var storageReference = FirebaseStorage.getInstance()

    suspend fun readUri(data: List<Dish>, getUrisCallback: GetUrisCallback, size: UriSize){
        val flow1 = MutableStateFlow(0)
        val uriDishList: MutableList<Dish> = mutableListOf()
        Log.d(my_tag, "size: ${data.size}")

        data.forEach {dish ->
            storageReference
                .getReference("dish_images/${dish.name}_${dish.id}_${size.uSize}.jpg")
                .downloadUrl
                .addOnSuccessListener {uri ->
                    if(size == UriSize.Small){
                        uriDishList.add(dish.copy(uriSmall = uri.toString()))
                        Log.d(my_tag, "SMALL in success: ${dish.id}")
                        flow1.value += 1
                    } else {
                        uriDishList.add(dish.copy(uriBig = uri.toString()))
                        Log.d(my_tag, "BIG in success: ${dish.id}")
                        flow1.value += 1
                    }

                }
        }
        flow1.collect{
            if(it == data.size){
                Log.d(my_tag, "after all")
                getUrisCallback.getUris(uriDishList)
                flow1.value = 0
            }
        }
    }
}