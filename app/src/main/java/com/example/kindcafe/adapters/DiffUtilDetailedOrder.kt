package com.example.kindcafe.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.kindcafe.data.DetailedOrderItem
import com.example.kindcafe.database.Dish

class DiffUtilDetailedOrder(
    private val oldList: List<DetailedOrderItem>,
    private val newList: List<DetailedOrderItem>
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
        return when {
            oldList[oldItemPosition].id != newList[newItemPosition].id -> false
            oldList[oldItemPosition].name != newList[newItemPosition].name -> false
            oldList[oldItemPosition].price != newList[newItemPosition].price -> false
            oldList[oldItemPosition].description != newList[newItemPosition].description -> false
            oldList[oldItemPosition].category != newList[newItemPosition].category -> false
            oldList[oldItemPosition].characteristic != newList[newItemPosition].characteristic -> false
            oldList[oldItemPosition].uriSmall != newList[newItemPosition].uriSmall -> false
            oldList[oldItemPosition].uriBig != newList[newItemPosition].uriBig -> false
            oldList[oldItemPosition].add1 != newList[newItemPosition].add1 -> false
            oldList[oldItemPosition].add2 != newList[newItemPosition].add2 -> false
            oldList[oldItemPosition].add3 != newList[newItemPosition].add3 -> false
            oldList[oldItemPosition].size != newList[newItemPosition].size -> false
            oldList[oldItemPosition].count != newList[newItemPosition].count -> false
            else -> true
        }
    }
}