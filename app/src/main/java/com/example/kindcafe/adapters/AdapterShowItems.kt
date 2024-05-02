package com.example.kindcafe.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kindcafe.database.Dish
import com.example.kindcafe.databinding.ItemChooseRvBinding
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class AdapterShowItems: RecyclerView.Adapter<AdapterShowItems.ViewHolderMy>() {

    private val my_tag = "AdapterShowItemsTag"
    private var oldDishList = emptyList<Dish>()
    private var storageReference = FirebaseStorage.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMy {
        val bindingOuter = ItemChooseRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderMy(bindingOuter, storageReference)
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

    class ViewHolderMy(val bindingInner: ItemChooseRvBinding, val fireStorage: FirebaseStorage): RecyclerView.ViewHolder(bindingInner.root) {
        private val my_tag_inner = "ItemTag"
        fun setData(data: Dish){

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
    }
}