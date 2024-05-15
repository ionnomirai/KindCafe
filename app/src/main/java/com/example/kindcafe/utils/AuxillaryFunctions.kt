package com.example.kindcafe.utils

import android.app.Activity
import android.content.res.ColorStateList
import android.util.Log
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.adapters.callbacks.ItemMoveDirections
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.Favorites
import com.example.kindcafe.database.OrderItem
import com.example.kindcafe.dialogs.DialogDetailedDish
import com.example.kindcafe.firebase.firebaseInterfaces.DefinitionOfStatus
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
                    //val action = ShowItemsFragmentDirections.actionShowItemsFragmentToDetailFragment(dish.id, dish.name)
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
}