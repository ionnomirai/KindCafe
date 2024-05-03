package com.example.kindcafe.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kindcafe.adapters.callbacks.ItemMoveDirections
import com.example.kindcafe.data.Categories
import com.example.kindcafe.data.SparklingDrinksAdditive
import com.example.kindcafe.database.Dish
import com.example.kindcafe.databinding.ItemBasketRvBinding
import com.example.kindcafe.databinding.ItemChooseRvBinding
import com.squareup.picasso.Picasso

class AdapterBasket(
    val itemMoveDirections: ItemMoveDirections
): RecyclerView.Adapter<AdapterBasket.ViewHolderBasket>() {

    private val my_tag = "AdapterShowItemsTag"
    private var oldDishList = emptyList<Dish>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBasket {
        val bindingOuter = ItemBasketRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderBasket(bindingOuter, itemMoveDirections)
    }

    override fun onBindViewHolder(holder: ViewHolderBasket, position: Int) {
        holder.setData(oldDishList[position])
    }

    override fun getItemCount(): Int {
        return oldDishList.size
    }

    fun setNewData(newDishList: List<Dish>){
        val diffUtilMy = DiffUtilDish(oldDishList, newDishList)
        val diffResults = DiffUtil.calculateDiff(diffUtilMy)
        oldDishList = newDishList
        diffResults.dispatchUpdatesTo(this)
    }

    class ViewHolderBasket(val bindingInner: ItemBasketRvBinding, val itemMoveDirectionsInner: ItemMoveDirections): RecyclerView.ViewHolder(bindingInner.root) {
        private val my_tag_inner = "ItemTag"

        fun setData(data: Dish){
            when(data.category){
                Categories.SparklingDrinks.categoryName -> { setDataLikeSparkling() }
                Categories.NonSparklingDrinks.categoryName -> {}
                Categories.Sweets.categoryName -> {}
                Categories.Cakes.categoryName -> {}
            }

            bindingInner.apply {
                tvItemName.text = data.name
                tvItemPrice.text = data.price
                try {
                    Picasso.get().load(data.uriSmall).into(ivItemPhoto)
                }
                catch (e: Exception){
                    Log.d(my_tag_inner, e.toString())
                }
                //Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/kindcafe-5c04a.appspot.com/o/dish_images%2FFanta_2.png?alt=media&token=030b0397-2fe3-44b0-ac28-2d1bbe6e47cc").into(ivItemPhoto)
            }
        }

        private fun setDataLikeSparkling(){
            bindingInner.apply {
                tvAdd1.text = SparklingDrinksAdditive.Lemon.name
                tvAdd2.text = SparklingDrinksAdditive.Ice.name
                tvAdd3.text = SparklingDrinksAdditive.NoSugar.name
            }
        }


    }
}