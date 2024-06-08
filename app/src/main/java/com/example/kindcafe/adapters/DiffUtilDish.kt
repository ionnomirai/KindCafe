package com.example.kindcafe.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.kindcafe.database.Dish

class DiffUtilDish(
    private val oldList: List<Dish>,
    private val newList: List<Dish>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when{
            oldList[oldItemPosition].id != newList[newItemPosition].id -> false
            oldList[oldItemPosition].name != newList[newItemPosition].name -> false
            oldList[oldItemPosition].price != newList[newItemPosition].price -> false
            oldList[oldItemPosition].description != newList[newItemPosition].description -> false
            oldList[oldItemPosition].category != newList[newItemPosition].category -> false
            oldList[oldItemPosition].characteristic != newList[newItemPosition].characteristic -> false
            oldList[oldItemPosition].uriSmall != newList[newItemPosition].uriSmall -> false
            oldList[oldItemPosition].uriBig != newList[newItemPosition].uriBig -> false
            else -> true
        }
    }
}