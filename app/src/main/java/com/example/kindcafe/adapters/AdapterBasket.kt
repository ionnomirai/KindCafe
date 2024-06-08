package com.example.kindcafe.adapters

import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kindcafe.R
import com.example.kindcafe.adapters.callbacks.ItemMoveDirections
import com.example.kindcafe.adapters.callbacks.SettingOrder
import com.example.kindcafe.data.CakesAdditive
import com.example.kindcafe.data.Categories
import com.example.kindcafe.data.DetailedOrderItem
import com.example.kindcafe.data.NonSparklingAdditive
import com.example.kindcafe.data.NumberAdd
import com.example.kindcafe.data.Size
import com.example.kindcafe.data.SparklingDrinksAdditive
import com.example.kindcafe.data.SweetsAdditive
import com.example.kindcafe.database.Favorites
import com.example.kindcafe.databinding.ItemBasketRvBinding
import com.example.kindcafe.utils.IconsOnItem
import com.example.kindcafe.utils.QuantityActions
import com.squareup.picasso.Picasso

class AdapterBasket(
    val itemMoveDirections: ItemMoveDirections,
    val orderSetting: SettingOrder
) : RecyclerView.Adapter<AdapterBasket.ViewHolderBasket>() {

    private val my_tag = "AdapterShowItemsTag"
    private var oldDishList = emptyList<DetailedOrderItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBasket {
        val bindingOuter =
            ItemBasketRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Log.d(my_tag, "onCreateViewHolder")
        return ViewHolderBasket(bindingOuter, itemMoveDirections, orderSetting)
    }

    override fun onBindViewHolder(holder: ViewHolderBasket, position: Int) {
        Log.d(my_tag, "onBindViewHolder")
        holder.setData(oldDishList[position])
    }

    override fun getItemCount(): Int {
        Log.d(my_tag, "getItemCount")
        return oldDishList.size
    }

    fun setNewData(newDishList: List<DetailedOrderItem>) {
        val diffUtilMy = DiffUtilDetailedOrder(oldDishList, newDishList)
        val diffResults = DiffUtil.calculateDiff(diffUtilMy)
        oldDishList = newDishList
        diffResults.dispatchUpdatesTo(this)
    }

    fun getCurrentList(): List<DetailedOrderItem>{
        return oldDishList
    }

    class ViewHolderBasket(
        val bindingInner: ItemBasketRvBinding,
        val itemMoveDirectionsInner: ItemMoveDirections,
        val ordSetting: SettingOrder
    ) : RecyclerView.ViewHolder(bindingInner.root) {
        private val my_tag_inner = "ItemTag"

        fun setData(data: DetailedOrderItem) {
            val currentFav = Favorites(data.id, data.id, data.name)

            bindingInner.apply {

                ibLike.setImageResource(R.drawable.ic_heart)
                setTintIcons(false)


                when (data.category) {
                    Categories.SparklingDrinks.categoryName -> {
                        setDataLikeSparkling()
                    }

                    Categories.NonSparklingDrinks.categoryName -> {
                        setDataLikeNonSparkling()
                    }

                    Categories.Sweets.categoryName -> {
                        setDataLikeSweets()
                    }

                    Categories.Cakes.categoryName -> {
                        setDataLikeCakes()
                    }
                }

                if (itemMoveDirectionsInner.checkUserExist()) {
                    // checking favorites list
                    if (itemMoveDirectionsInner.checkFavorites(currentFav)) {
                        Log.d(my_tag_inner, "data fav: ${data}")
                        bindingInner.ibLike.setImageResource(R.drawable.ic_heart_filled)
                        setTintIcons(true)
                    }

                    Log.d(
                        my_tag_inner, "currentItem: ${data.id} ${data.name}; "
                                + "add1 = ${data.add1}; add2 = ${data.add2}; add1 = ${data.add3}; siz3 = ${data.size};"
                                + " quantity ${data.count}"
                    )

                    /* setup background tvSize depending order size */
                    checkSize(data.size, listOf(tvAdd4, tvAdd5, tvAdd6))

                    /* setup background tvAdditivies depending user choice */
                    checkAdditives(listOf(data.add1, data.add2, data.add3))

                }

                // add to favorite all del from favorite
                ibLike.setOnClickListener {
                    if (itemMoveDirectionsInner.checkFavorites(currentFav)) {
                        itemMoveDirectionsInner.delFromFavorite(currentFav)
                        ibLike.setImageResource(R.drawable.ic_heart)
                        setTintIcons(false)

                    } else {
                        itemMoveDirectionsInner.putToFavorite(
                            Favorites(
                                id = data.id,
                                dishId = data.id,
                                dishName = data.name
                            )
                        )
                        ibLike.setImageResource(R.drawable.ic_heart_filled)
                        setTintIcons(true)
                    }
                }

                // handle ADDICTION and SUBTRACTION clicks
                data.name?.let { iName ->
                    ibSubtract.setOnClickListener {
                        ordSetting.setQuantity(
                            data.id, iName, changeQuantity(data.count, QuantityActions.SUBTRACTION)
                        )
                    }
                    ibPlus.setOnClickListener {
                        ordSetting.setQuantity(
                            data.id, iName, changeQuantity(data.count, QuantityActions.ADDITION)
                        )
                    }
                }

                // User can change additive for the order item
                cvAdd1.setOnClickListener {
                    //adapter.notifyItemChanged(adapterPosition)
                    data.name?.let {
                        ordSetting.setAdds(
                            id = data.id,
                            name = it,
                            addNumber = NumberAdd.ADD1,
                            value = isAddNull(data.add1)
                        )
                    }
                }
                cvAdd2.setOnClickListener {
                    data.name?.let {
                        ordSetting.setAdds(
                            id = data.id,
                            name = it,
                            addNumber = NumberAdd.ADD2,
                            value = isAddNull(data.add2)
                        )
                    }
                }
                cvAdd3.setOnClickListener {
                    data.name?.let {
                        ordSetting.setAdds(
                            id = data.id,
                            name = it,
                            addNumber = NumberAdd.ADD3,
                            value = isAddNull(data.add3)
                        )
                    }
                }

                // User can change order item size
                cvAdd4.setOnClickListener {
                    data.name?.let { ordSetting.setSize(data.id, it, Size.Small) }
                }
                cvAdd5.setOnClickListener {
                    data.name?.let { ordSetting.setSize(data.id, it, Size.Medium) }
                }
                cvAdd6.setOnClickListener {
                    data.name?.let { ordSetting.setSize(data.id, it, Size.Big) }
                }





                tvItemName.text = data.name
                //tvItemPrice.text = data.price
                tvItemPrice.text = bindingInner.root.context.getString(R.string.price_style_usd, data.price)
                tvItemName.setHorizontallyScrolling(true)
                tvItemName.movementMethod = ScrollingMovementMethod()

                tvDrop.setHorizontallyScrolling(true)
                tvDrop.movementMethod = ScrollingMovementMethod()
                tvCount.text = data.count ?: "0"
                try {
                    Picasso.get().load(data.uriSmall).into(ivItemPhoto)
                } catch (e: Exception) {
                    Log.d(my_tag_inner, e.toString())
                }
                //Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/kindcafe-5c04a.appspot.com/o/dish_images%2FFanta_2.png?alt=media&token=030b0397-2fe3-44b0-ac28-2d1bbe6e47cc").into(ivItemPhoto)
            }
        }

        private fun setDataLikeSparkling() {
            bindingInner.apply {
                tvAdd1.text = SparklingDrinksAdditive.LEMON.addsName
                tvAdd2.text = SparklingDrinksAdditive.ICE.addsName
                tvAdd3.text = SparklingDrinksAdditive.NO_SUGAR.addsName
            }
        }

        private fun setDataLikeNonSparkling() {
            bindingInner.apply {
                tvAdd1.text = NonSparklingAdditive.MILK.addsName
                tvAdd2.text = NonSparklingAdditive.ICE.addsName
                tvAdd3.text = NonSparklingAdditive.SUGAR.addsName
            }
        }

        private fun setDataLikeCakes() {
            bindingInner.apply {
                tvAdd1.text = CakesAdditive.FRUITS.addsName
                tvAdd2.text = CakesAdditive.NUTS.addsName
                tvAdd3.text = CakesAdditive.SYRUP.addsName
            }
        }

        private fun setDataLikeSweets() {
            bindingInner.apply {
                tvAdd1.text = SweetsAdditive.MILK.addsName
                tvAdd2.text = SweetsAdditive.NUTS.addsName
                tvAdd3.text = SweetsAdditive.RAISIN.addsName
            }
        }

        private fun setTintIcons(isPress: Boolean) {
            bindingInner.apply {
                val colorStateList = itemMoveDirectionsInner.getTint(isPress)
                colorStateList?.let { ibLike.imageTintList = it }
            }
        }

        /* Check size and set grey color for active size button */
        private fun checkSize(size: String?, list: List<TextView>) {
            bindingInner.apply {
                when (size) {
                    Size.Small.name, null -> setBackSize(list, Size.Small.ordinal)
                    Size.Medium.name -> setBackSize(list, Size.Medium.ordinal)
                    Size.Big.name -> setBackSize(list, Size.Big.ordinal)
                }
            }
        }

        // It is need only for fun checkSize. This function change back active size card.
        private fun setBackSize(list: List<TextView>, indexActive: Int) {
            for (i in list.indices) {
                if (i == indexActive) {
                    //Log.d(my_tag_inner, "i == indexActive; IndexActive = $indexActive, i = $i, listSize = ${list.size}")
                    list[i].setBackgroundResource(R.color.black_25_transparent)
                } else {
                    //Log.d(my_tag_inner, "i != indexActive; IndexActive = $indexActive, i = $i, listSize = ${list.size}")
                    list[i].setBackgroundResource(R.drawable.back_additioanl_card)
                }
            }
        }

        private fun isAddNull(addN: Boolean?): Boolean {
            return if (addN == null || addN == false) true else false
        }

        private fun checkAdditives(adds: List<Boolean?>) {
            bindingInner.apply {
                adds.forEachIndexed { index, value ->
                    if (value != null && value == true) {
                        when (index) {
                            0 -> tvAdd1.setBackgroundResource(R.color.black_25_transparent)
                            1 -> tvAdd2.setBackgroundResource(R.color.black_25_transparent)
                            2 -> tvAdd3.setBackgroundResource(R.color.black_25_transparent)
                        }
                    } else {
                        when (index) {
                            0 -> tvAdd1.setBackgroundResource(R.drawable.back_additioanl_card)
                            1 -> tvAdd2.setBackgroundResource(R.drawable.back_additioanl_card)
                            2 -> tvAdd3.setBackgroundResource(R.drawable.back_additioanl_card)
                        }
                    }
                }
            }
        }

        private fun changeQuantity(count: String?, action: QuantityActions): String {
            bindingInner.apply {
                if (action == QuantityActions.SUBTRACTION) {
                    if (count == null || count == "0") {
                        return "0"
                    }
                    return (count.toInt() - 1).toString()
                } else {
                    if (count == null || count == "0") {
                        return "1"
                    }
                    if (count == "20") {
                        return "20"
                    }
                    return (count.toInt() + 1).toString()
                }
            }
        }

    }
}