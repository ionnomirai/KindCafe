package com.example.kindcafe.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.adapters.AdapterShowItems
import com.example.kindcafe.adapters.callbacks.ItemMoveDirections
import com.example.kindcafe.data.Categories
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.Favorites
import com.example.kindcafe.database.OrderItem
import com.example.kindcafe.databinding.FragFavoritesBinding
import com.example.kindcafe.databinding.FragHomeBinding
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragFavoritesBinding? = null
    private val binding
        get() : FragFavoritesBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    /* Common viewModel between activity and this fragment */
    private val mainVM: MainViewModel by activityViewModels()
    private val myAdapter = AdapterShowItems(clickItemElements())
    private val dbManager = DbManager()

    private val my_tag = "FavoriteFragmentTag"
    private val currentFragmentName = "Favorites"

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragFavoritesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(my_tag, "onViewCreated")

        val mainAct = activity as? MainActivity

        mainAct?.let {
            it.binding.tvToolbarTitle.text = currentFragmentName
            it.supportActionBar?.title = ""
        }

        binding.rvShowItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myAdapter
        }

        // Get all favorites like Favorites
        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.getAllFavorites()
            Log.d(my_tag, "after got all favorites")
            cancel()
        }

        // Transform Favorites like Dish
        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.favorites.collect {
                Log.d(my_tag, "favorites: $it")
                val dl = mutableListOf<Dish>()
                it.forEach {
                    dl.add(mainVM.getLikeDish(it.dishId!!, it.dishName!!))
                    Log.d(my_tag, "favorites inside trans: $it")
                }
                mainVM.addToFavLikeDish(dl)
            }
        }

        // Update adapter
        viewLifecycleOwner.lifecycleScope.launch{
            mainVM.favoritesLikeDish.collect{
                Log.d(my_tag, "setNewData: $it")
                myAdapter.setNewData(it)
            }
        }




    }

    private fun clickItemElements(): ItemMoveDirections {
        return object : ItemMoveDirections {
            override fun detailed(dish: Dish) {
                dish.name?.let {
                    val action =
                        FavoriteFragmentDirections.actionFavoriteFragmentToDetailFragment(
                            dish.id,
                            dish.name
                        )
                    findNavController().navigate(action)
                }
            }

            override fun putToBag(dish: Dish) {
                viewLifecycleOwner.lifecycleScope.launch{
                    val orderObj = OrderItem(id = dish.id, name = dish.name)
                    mainVM.addOrderItemsLocal(orderObj)
                    dbManager.setOrderItemBasketToRDB(KindCafeApplication.myAuth.currentUser, orderObj)
                    cancel()
                }
            }

            override fun delFromBag(dish: Dish) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val cur = mainVM.orderBasket.value.find { (it.id == dish.id && it.name == dish.name) }
                    cur?.let {oItem ->
                        mainVM.deleteOrderItemsLocal(oItem)
                        dbManager.deleteOrderBasketItemFromRDB(KindCafeApplication.myAuth.currentUser, oItem)

                    }
                    cancel()
                }
            }

            override fun putToFavorite(favoriteDish: Favorites) {
/*                viewLifecycleOwner.lifecycleScope.launch {
                    mainVM.addFavoritesLocal(favoriteDish)
                    dbManager.setFavoriteDishes(KindCafeApplication.myAuth.currentUser, favoriteDish)
                    cancel()
                }*/
            }

            override fun delFromFavorite(favoriteDish: Favorites) {
                viewLifecycleOwner.lifecycleScope.launch {
                    mainVM.deleteFavDish(favoriteDish)
                    dbManager.deleteFavoriteDish(KindCafeApplication.myAuth.currentUser, favoriteDish)
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
                context?.let {
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

    override fun onResume() {
        super.onResume()
        Log.d(my_tag, "onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}