package com.example.kindcafe.utils

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.adapters.callbacks.ItemMoveDirections
import com.example.kindcafe.adapters.callbacks.SettingOrder
import com.example.kindcafe.data.DetailedOrderItem
import com.example.kindcafe.data.NumberAdd
import com.example.kindcafe.data.Size
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.Favorites
import com.example.kindcafe.database.OrderItem
import com.example.kindcafe.dialogs.DialogDetailedDish
import com.example.kindcafe.firebase.firebaseInterfaces.DefinitionOfStatus
import com.example.kindcafe.interfaces.SwipeBasketItem
import com.example.kindcafe.viewModels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

object AuxillaryFunctions {
private val my_tag = "AuxillaryFunctionsTAG"
    /* Show message on the screen */
    fun showSnackBar(
        @StringRes messageText: Int,
        activity: Activity,
        @IdRes currentView: Int = R.id.lDrawLayoutMain,
        @StringRes closeButton: Int = R.string.close
    ){
        val snackbar: Snackbar = Snackbar.make(
            activity.findViewById(currentView),
            activity.resources.getText(messageText),
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar
            .setAction(closeButton) {
                snackbar.dismiss()
            }
            .show()
    }

    fun defaultDefinitionOfStatusInterface(
        frag: Fragment,
        direction: SimplePopDirections,
        @IdRes fragContainer: Int = R.id.fcv_main): DefinitionOfStatus{
        return object : DefinitionOfStatus{
            override fun onSuccess() {
                try {
                    (frag.activity as MainActivity).doWhenStartOrLogin()
                    val curFrag = frag.parentFragmentManager.findFragmentById(fragContainer)
                    when(direction){
                        SimplePopDirections.TOP_DESTINATION -> curFrag?.findNavController()?.popBackStack(R.id.homeFragment, false)
                        SimplePopDirections.PREVIOUS_DESTINATION -> curFrag?.findNavController()?.popBackStack()
                    }
                } catch (e: Exception){
                    /* If user close screen earlier than it would auto*/
                    Log.d(my_tag, "LoginFrag exception: $e")
                }
            }
        }
    }


    fun deafultItemMoveDirections(frag: Fragment, mainVM: MainViewModel, actionDetailed: ((Dish) -> NavDirections)?): ItemMoveDirections{
        return object : ItemMoveDirections {
            override fun detailed(dish: Dish) {
                dish.name?.let {
                    if (actionDetailed != null) {
                        frag.findNavController().navigate(actionDetailed(dish))
                    } else {
                        DialogDetailedDish(dish).show(frag.parentFragmentManager, null)
                    }
                }
            }

            override fun putToBag(dish: Dish) {
                frag.viewLifecycleOwner.lifecycleScope.launch{
                    val orderObj = OrderItem(id = dish.id, name = dish.name)
                    mainVM.addOrderItemsLocal(orderObj)
                    mainVM.dbManager.setOrderItemBasketToRDB(KindCafeApplication.myAuth.currentUser, orderObj)
                    cancel()
                }
            }

            override fun delFromBag(dish: Dish) {
                frag.viewLifecycleOwner.lifecycleScope.launch {
                    val cur = mainVM.orderBasket.value.find { (it.id == dish.id && it.name == dish.name) }
                    cur?.let {oItem ->
                        mainVM.deleteOrderItemsLocal(oItem)
                        mainVM.dbManager.deleteOrderBasketItemFromRDB(KindCafeApplication.myAuth.currentUser, oItem)
                    }
                    cancel()
                }
            }

            override fun putToFavorite(favoriteDish: Favorites) {
                frag.viewLifecycleOwner.lifecycleScope.launch{
                    mainVM.addFavoritesLocal(favoriteDish)
                    mainVM.dbManager.setFavoriteDishes(KindCafeApplication.myAuth.currentUser, favoriteDish)
                    // delete after all
                    mainVM.getAllFavorites()
                    cancel()
                }
            }

            override fun delFromFavorite(favoriteDish: Favorites) {
                frag.viewLifecycleOwner.lifecycleScope.launch {
                    mainVM.deleteFavDish(favoriteDish)
                    mainVM.dbManager.deleteFavoriteDish(KindCafeApplication.myAuth.currentUser, favoriteDish)
                    mainVM.getAllFavorites()
                    cancel()
                }
            }

            override fun checkFavorites(favoriteDish: Favorites): Boolean {
                return favoriteDish in mainVM.favorites.value
            }

            override fun checkUserExist(): Boolean {
                return KindCafeApplication.myAuth.currentUser != null
            }

            override fun getTint(isPress: Boolean): ColorStateList? {
                frag.context?.let {
                    if(isPress){
                        return AppCompatResources.getColorStateList(it, R.color.greeting_phrase_color)
                    }
                    return AppCompatResources.getColorStateList(it, R.color.item_icon)
                }
                return null
            }

            // if true, dish in bag
            override fun checkBag(dish: Dish): Boolean {
                return mainVM.orderBasket.value.filter { (it.id == dish.id && it.name == dish.name) }.isNotEmpty()
            }
        }
    }

    fun defaultClickSettingOrder(frag: Fragment, mainVM: MainViewModel): SettingOrder {
        return object : SettingOrder {
            override fun setQuantity(id: String, name: String, quantity: String) {
                frag.viewLifecycleOwner.lifecycleScope.launch {
                    val newOrderQ = mainVM.orderBasket.value.find { (it.id == id && it.name == name) }
                        ?.copy(count = quantity)
                    newOrderQ?.let { mainVM.addOrderItemsLocal(it) }
                    Log.d(my_tag,"set quantity")
                    cancel()
                }
            }

            override fun setSize(id: String, name: String, size: Size) {
                frag.viewLifecycleOwner.lifecycleScope.launch {
                    val newOrderI = mainVM.orderBasket.value.find { (it.id == id && it.name == name) }
                        ?.copy(size = size.name)
                    //Log.d(my_tag, newOrderI.toString())
                    newOrderI?.let { mainVM.addOrderItemsLocal(it) }
                    Log.d(my_tag,"set size")
                    cancel()
                }
            }

            override fun setAdds(id: String, name: String, addNumber: NumberAdd, value: Boolean) {
                val newOrderI = mainVM.orderBasket.value.find { (it.id == id && it.name == name) }

                frag.viewLifecycleOwner.lifecycleScope.launch {
                    newOrderI?.let {
                        when(addNumber){
                            NumberAdd.ADD1 -> mainVM.addOrderItemsLocal(it.copy(add1 = value))
                            NumberAdd.ADD2 -> mainVM.addOrderItemsLocal(it.copy(add2 = value))
                            NumberAdd.ADD3 -> mainVM.addOrderItemsLocal(it.copy(add3 = value))
                        }
                    }
                    Log.d(my_tag,"set add")
                    cancel()
                }
            }
        }
    }

    fun transformOrdItemToDishesDetailed(orderItems: List<OrderItem>, allDishes: List<Dish>): List<DetailedOrderItem>{
        val tempDishes = mutableListOf<Dish>()

        orderItems.forEachIndexed { index, orderItem ->
            val dish = allDishes.find{(it.id == orderItem.id && it.name == orderItem.name)}
            dish?.let { tempDishes.add(it) }
        }

        return List(tempDishes.size){index ->
            DetailedOrderItem(
                id = tempDishes[index].id,
                name = tempDishes[index].name,
                price = tempDishes[index].price,
                description = tempDishes[index].description,
                category = tempDishes[index].category,
                characteristic = tempDishes[index].characteristic,
                uriSmall = tempDishes[index].uriSmall,
                uriBig = tempDishes[index].uriBig,
                add1 = orderItems[index].add1,
                add2 = orderItems[index].add2,
                add3 = orderItems[index].add3,
                size = orderItems[index].size,
                count = orderItems[index].count
            )
        }
    }

    fun defaultSwipeDelBasketOrder(
        deleteIcon: Drawable,
        swipeBackground: ColorDrawable,
        mainVM: MainViewModel? = null,
        swipeBasketItem: SwipeBasketItem? = null
    ): ItemTouchHelper.SimpleCallback{
        return object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean { return false }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                /* position of element that swiped */
                val position = viewHolder.adapterPosition
                swipeBasketItem?.getDelPosition(position)

                if(swipeBasketItem == null){
                    val item = mainVM?.orderBasket?.value?.get(position) ?: OrderItem()
                    mainVM?.viewModelScope?.launch {
                        mainVM.deleteOrderItemsLocal(item)
                        mainVM.dbManager.deleteOrderBasketItemFromRDB(
                            KindCafeApplication.myAuth.currentUser,
                            item
                        )
                        cancel()
                    }
                }

            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

                if(dX > 0) {
                    swipeBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                    deleteIcon.setBounds(
                        itemView.left + iconMargin - 150,
                        itemView.top + iconMargin,
                        itemView.left + iconMargin + deleteIcon.intrinsicWidth,
                        itemView.bottom - iconMargin)
                } else {
                    swipeBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon.setBounds(
                        itemView.right - iconMargin - deleteIcon.intrinsicWidth + 125,
                        itemView.top + iconMargin,
                        itemView.right - iconMargin + 275,
                        itemView.bottom - iconMargin)
                }

                swipeBackground.draw(c)
                c.save()


                if(dX > 0){
                    c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                } else {
                    c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                }
                deleteIcon.draw(c)
                c.restore()

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
    }
}