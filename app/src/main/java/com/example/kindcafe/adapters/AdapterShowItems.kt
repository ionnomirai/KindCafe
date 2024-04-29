package com.example.kindcafe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kindcafe.data.Dish
import com.example.kindcafe.databinding.ItemChooseRvBinding

class AdapterShowItems: RecyclerView.Adapter<AdapterShowItems.ViewHolderMy>() {

    private var oldDishList = emptyList<Dish>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMy {
        val bindingOuter = ItemChooseRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderMy(bindingOuter)
    }

    override fun onBindViewHolder(holder: ViewHolderMy, position: Int) {
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

    class ViewHolderMy(val bindingInner: ItemChooseRvBinding): RecyclerView.ViewHolder(bindingInner.root) {
        fun setData(data: Dish){
            bindingInner.apply {
                tvItemName.text = data.name
                tvItemPrice.text = data.price
            }
        }
    }
}