package com.example.kindcafe.adapters

import android.app.Activity
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kindcafe.R
import com.example.kindcafe.adapters.callbacks.ItemMoveDirections
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.Favorites
import com.example.kindcafe.databinding.ItemChooseRvBinding
import com.example.kindcafe.utils.IconsOnItem
import com.squareup.picasso.Picasso

class AdapterShowItems(
    val itemMoveDirections: ItemMoveDirections
): RecyclerView.Adapter<AdapterShowItems.ViewHolderMy>() {

    private val my_tag = "AdapterShowItemsTag"
    private var oldDishList = emptyList<Dish>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMy {
        val bindingOuter = ItemChooseRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderMy(bindingOuter, itemMoveDirections)
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

    class ViewHolderMy(val bindingInner: ItemChooseRvBinding, val itemMoveDirectionsInner: ItemMoveDirections): RecyclerView.ViewHolder(bindingInner.root) {
        private val my_tag_inner = "ItemTag"
        fun setData(data: Dish){
            bindingInner.apply {
                tvItemName.text = data.name
                tvItemPrice.text = data.price

                val currentFav = Favorites(data.id, data.id, data.name)

                try { Picasso.get().load(data.uriSmall).into(ivItemPhoto) }
                catch (e: Exception){ Log.d(my_tag_inner, e.toString()) }
                //Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/kindcafe-5c04a.appspot.com/o/dish_images%2FFanta_2.png?alt=media&token=030b0397-2fe3-44b0-ac28-2d1bbe6e47cc").into(ivItemPhoto)

                /* Allow these functions only when user login */
                if(itemMoveDirectionsInner.checkUserExist()){
                    if (itemMoveDirectionsInner.checkFavorites(currentFav)){
                        Log.d(my_tag_inner, "data fav: ${data}")
                        bindingInner.ibLike.setImageResource(R.drawable.ic_heart_filled)
                        setTintIcons(IconsOnItem.FAVORITE, true)
                    }

                    if (itemMoveDirectionsInner.checkBag(data)){
                        setTintIcons(IconsOnItem.BAG, true)
                    }

                    ibLike.setOnClickListener {
                        if(itemMoveDirectionsInner.checkFavorites(currentFav)){
                            itemMoveDirectionsInner.delFromFavorite(currentFav)
                            ibLike.setImageResource(R.drawable.ic_heart)
                            setTintIcons(IconsOnItem.FAVORITE, false)

                        } else {
                            itemMoveDirectionsInner.putToFavorite(Favorites( id = data.id, dishId = data.id, dishName = data.name))
                            ibLike.setImageResource(R.drawable.ic_heart_filled)
                            setTintIcons(IconsOnItem.FAVORITE, true)

                        }
                    }

                    ibPutToOrder.setOnClickListener {
                        if(itemMoveDirectionsInner.checkBag(data)){
                            itemMoveDirectionsInner.delFromBag(data)
                            setTintIcons(IconsOnItem.BAG, false)
                        } else {
                            itemMoveDirectionsInner.putToBag(data)
                            setTintIcons(IconsOnItem.BAG, true)
                        }

                    }
                }

                tvItemInfo.setOnClickListener {
                    itemMoveDirectionsInner.detailed(data)
                }



            }
        }

        private fun setTintIcons(ic: IconsOnItem, isPress: Boolean){
            bindingInner.apply {
                val colorStateList = itemMoveDirectionsInner.getTint(isPress)
                colorStateList?.let {
                    when(ic){
                        IconsOnItem.BAG -> ibPutToOrder.imageTintList = it
                        IconsOnItem.FAVORITE -> ibLike.imageTintList = it
                    }
                }
            }
        }
    }
}