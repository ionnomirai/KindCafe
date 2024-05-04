package com.example.kindcafe.firebase

import android.util.Log
import com.example.kindcafe.data.AllUserData
import com.example.kindcafe.data.Categories
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.Favorites
import com.example.kindcafe.database.OrderItem
import com.example.kindcafe.database.UserPersonal
import com.example.kindcafe.firebase.firebaseEnums.CategoriesInUsers
import com.example.kindcafe.firebase.firebaseInterfaces.ReadAllData
import com.example.kindcafe.firebase.firebaseInterfaces.ReadAndSplitCategories
import com.example.kindcafe.firebase.firebaseInterfaces.ReadUsersData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {
    private val myTag = "DbManagerMy"
    private val myDatabase = Firebase.database.getReference("dishes")
    private val myDatabaseUser = Firebase.database.getReference("users")

    /* old variant*/
        fun readAllDishDataFromDb(callbackRead: ReadAllData) {
            //val result: MutableMap<Int, List<Dish>> = mutableMapOf()
            val result: MutableList<Dish> = mutableListOf()

            myDatabase.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    // We will enter into each category
                    for (i in Categories.entries) {
                        // list of dishes from current category (sparkling, non-sparkling etc.
                        //val dishList: MutableList<Dish> = mutableListOf()

                        // move to category. Every iteration outer cycle, will be different category
                        val deeperSnapshot = snapshot.child(i.name).children

                        // collect each dish
                        for (item in deeperSnapshot) {
                            item.getValue(Dish::class.java)?.let { result.add(it) }
                        }

                        // put to result map: key - index of category, value - lish of dish
                        //result[i.ordinal] = dishList
                    }

                    callbackRead.readAll(result)
                    Log.d(myTag, result.toString())
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

    fun readDishDataFromDb(category: Categories, callbackRead: ReadAndSplitCategories) {
        myDatabase.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // list of dishes from current category (sparkling, non-sparkling etc.
                val dishList: MutableList<Dish> = mutableListOf()

                // move to category. Every iteration outer cycle, will be different category
                val deeperSnapshot = snapshot.child(category.name).children

                //collect each dish
                for (item in deeperSnapshot) {
                    item.getValue(Dish::class.java)?.let { dishList.add(it) }
                }

                callbackRead.readAndSplit(dishList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /* Create personal package and put name there */
    fun setPrimaryData(user: FirebaseUser?, userPersonal: UserPersonal){
        user?.let {
            myDatabaseUser.child(it.uid)
                .child("personal")
                .setValue(userPersonal)
        }
    }

    fun readUsersData(user: FirebaseUser, callbackRead: ReadUsersData){
        myDatabaseUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val personalSnapshot = snapshot.child(user.uid).children
                val allCategories = AllUserData(mutableListOf(), mutableListOf())

                for (item in personalSnapshot) {
                    when(item.key){
                        CategoriesInUsers.FAVORITE.cName ->{
                            for (itemDeep in item.children){
                                itemDeep.getValue(Favorites::class.java)?.let { allCategories.favorites?.add(it) }
                            }
                        }
                        CategoriesInUsers.ORDER.cName -> {
                            for (itemDeep in item.children){
                                itemDeep.getValue(OrderItem::class.java)?.let { allCategories.order?.add(it) }
                            }
                        }
                        CategoriesInUsers.PERSONAL.cName -> item.getValue(UserPersonal::class.java)?.let { allCategories.personal = it }
                    }
                }
                callbackRead.readAllUserData(allCategories)
                Log.d(myTag, "All data: $allCategories")
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setFavoriteDishes(user: FirebaseUser?, fav: Favorites){
        user?.let {
            myDatabaseUser.child(it.uid)
                .child("favorites")
                .child("favorite_${fav.id}")
                .setValue(fav)
        }
    }

    fun deleteFavoriteDish(user: FirebaseUser?, fav: Favorites){
        user?.let {
            myDatabaseUser.child(it.uid)
                .child("favorites")
                .child("favorite_${fav.id}")
                .removeValue()
        }
    }

    fun setTestData(user: FirebaseUser?){
        user?.let {
            myDatabaseUser.child(it.uid)
                .child("favorites")
                .child("favItems2")
                .setValue(Favorites("2", "2", "Fanta"))

            myDatabaseUser.child(it.uid)
                .child("favorites")
                .child("favItems3")
                .setValue(Favorites("3", "3", "Pepsi"))

            myDatabaseUser.child(it.uid)
                .child("order")
                .child("orderItem1")
                .setValue(OrderItem("some order1"))

            myDatabaseUser.child(it.uid)
                .child("order")
                .child("orderItem2")
                .setValue(OrderItem("some order2"))
            myDatabaseUser.child(it.uid)
                .child("order")
                .child("orderItem2")
                .setValue(OrderItem("some order3"))
        }
    }


}