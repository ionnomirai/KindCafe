package com.example.kindcafe.firebase

import android.util.Log
import com.example.kindcafe.data.Categories
import com.example.kindcafe.data.Dish
import com.example.kindcafe.firebase.firebaseInterfaces.ReadAndSplitCategories
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {
    private val myTag = "DbManagerMy"
    private val myDatabase = Firebase.database.getReference("dishes")

    fun readDishDataFromDb(callbackRead: ReadAndSplitCategories){
        val result: MutableMap<Int, List<Dish>> = mutableMapOf()

        myDatabase.addListenerForSingleValueEvent(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                /* We will enter into each category */
                for(i in Categories.entries){
                    /* list of dishes from current category (sparkling, non-sparkling etc.*/
                    val dishList: MutableList<Dish> = mutableListOf()

                    /* move to category. Every iteration outer cycle, will be different category */
                    val deeperSnapshot = snapshot.child(i.name).children

                    /* collect each dish*/
                    for (item in deeperSnapshot){
                        item.getValue(Dish::class.java)?.let { dishList.add(it) }
                    }

                    /* put to result map: key - index of category, value - lish of dish */
                    result[i.ordinal] = dishList
                }
                callbackRead.readAndSplit(result)
                Log.d(myTag, result.toString())
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}