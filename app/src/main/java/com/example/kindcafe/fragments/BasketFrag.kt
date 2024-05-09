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
import com.example.kindcafe.adapters.AdapterBasket
import com.example.kindcafe.adapters.AdapterShowItems
import com.example.kindcafe.adapters.callbacks.ItemMoveDirections
import com.example.kindcafe.adapters.callbacks.SettingOrder
import com.example.kindcafe.data.Categories
import com.example.kindcafe.data.DetailedOrderItem
import com.example.kindcafe.data.NumberAdd
import com.example.kindcafe.data.Size
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.Favorites
import com.example.kindcafe.databinding.FragBasketBinding
import com.example.kindcafe.databinding.FragHomeBinding
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BasketFrag: Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragBasketBinding? = null
    private val binding
        get() : FragBasketBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    /* Common viewModel between activity and this fragment */
    private val mainVM: MainViewModel by activityViewModels()

    private val my_tag = "BasketFragmentTag"

    private val myAdapter = AdapterBasket(clickItemElements(), clickSettingOrder())
    private val dbManager = DbManager()

    private val tempDishes = mutableListOf<Dish>()
    private val detailedListI = mutableListOf<DetailedOrderItem>()

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragBasketBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvItemsBuy.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myAdapter
        }

        Log.d(my_tag, "basket: ${mainVM.orderBasket.value}")

        viewLifecycleOwner.lifecycleScope.launch{
            mainVM.getOrderItemsLocal()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.orderBasket.collect{
                tempDishes.clear()
                dbManager.setOrderBasketToRDB(KindCafeApplication.myAuth.currentUser, it)
                it.forEachIndexed{index, orderItem ->
                    val dish = mainVM.allDishes.value.find{(it.id == orderItem.id && it.name == orderItem.name)}
                    dish?.let { tempDishes.add(it) }
                }

                val detailedList = List(tempDishes.size){index ->
                    DetailedOrderItem(
                        id = tempDishes[index].id,
                        name = tempDishes[index].name,
                        price = tempDishes[index].price,
                        description = tempDishes[index].description,
                        category = tempDishes[index].category,
                        characteristic = tempDishes[index].characteristic,
                        uriSmall = tempDishes[index].uriSmall,
                        uriBig = tempDishes[index].uriBig,
                        add1 = mainVM.orderBasket.value[index].add1,
                        add2 = mainVM.orderBasket.value[index].add2,
                        add3 = mainVM.orderBasket.value[index].add3,
                        size = mainVM.orderBasket.value[index].size,
                        count = mainVM.orderBasket.value[index].count
                    )
                }

                detailedListI.clear()
                detailedListI.addAll(detailedList)

                Log.d(my_tag, "size detailed list: ${detailedList.size}")
                Log.d(my_tag, "list: ${detailedList}")
                myAdapter.setNewData(detailedList)
            }
        }

        binding.clButtonMakeOrder.setOnClickListener {
            val action = BasketFragDirections.actionBasketFragToOrderSummaryFragment(detailedListI.toTypedArray())
            findNavController().navigate(action)
        }

/*        mainVM.orderBasket.value.forEachIndexed{index, orderItem ->
            val dish = mainVM.allDishes.value.find{(it.id == orderItem.id && it.name == orderItem.name)}
            dish?.let { tempDishes.add(it) }
        }

        val detailedList = List(tempDishes.size){index ->
            DetailedOrderItem(
                id = tempDishes[index].id,
                name = tempDishes[index].name,
                price = tempDishes[index].price,
                description = tempDishes[index].description,
                category = tempDishes[index].category,
                characteristic = tempDishes[index].characteristic,
                uriSmall = tempDishes[index].uriSmall,
                uriBig = tempDishes[index].uriBig,
                add1 = mainVM.orderBasket.value[index].add1,
                add2 = mainVM.orderBasket.value[index].add2,
                add3 = mainVM.orderBasket.value[index].add3,
                size = mainVM.orderBasket.value[index].size,
                count = mainVM.orderBasket.value[index].count
            )
        }

        Log.d(my_tag, "size detailed list: $detailedList")
        myAdapter.setNewData(detailedList)*/

/*        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.allDishes.collect{
                myAdapter.setNewData(it)
            }
        }*/
    }

    override fun onResume() {
        super.onResume()
        Log.d(my_tag, "onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




    fun clickItemElements(): ItemMoveDirections{
        return object : ItemMoveDirections{
            override fun detailed(dish: Dish) {}

            override fun putToBag(dish: Dish) {}

            override fun putToFavorite(favoriteDish: Favorites) {
                Log.d(my_tag, "cuurent user: ${KindCafeApplication.myAuth.currentUser}")
                viewLifecycleOwner.lifecycleScope.launch{
                    mainVM.addFavoritesLocal(favoriteDish)
                    dbManager.setFavoriteDishes(KindCafeApplication.myAuth.currentUser, favoriteDish)
                    // delete after all
                    mainVM.getAllFavorites()
                    cancel()
                }
            }

            override fun delFromFavorite(favoriteDish: Favorites) {
                viewLifecycleOwner.lifecycleScope.launch {
                    mainVM.deleteFavDish(favoriteDish)
                    dbManager.deleteFavoriteDish(KindCafeApplication.myAuth.currentUser, favoriteDish)
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
                context?.let {
                    if(isPress){
                        return AppCompatResources.getColorStateList(it, R.color.greeting_phrase_color)
                    }
                    return AppCompatResources.getColorStateList(it, R.color.item_icon)
                }
                return null
            }

            override fun delFromBag(dish: Dish) {}

            override fun checkBag(dish: Dish): Boolean {
                return mainVM.orderBasket.value.filter { (it.id == dish.id && it.name == dish.name) }.isNotEmpty()
            }
        }
    }

    fun clickSettingOrder(): SettingOrder{
        return object : SettingOrder{
            override fun setQuantity(id: String, name: String, quantity: String) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val newOrderQ = mainVM.orderBasket.value.find { (it.id == id && it.name == name) }
                        ?.copy(count = quantity)
                    newOrderQ?.let { mainVM.addOrderItemsLocal(it) }
                    Log.d(my_tag,"set quantity")
                    cancel()
                }
            }

            override fun setSize(id: String, name: String, size: Size) {
                viewLifecycleOwner.lifecycleScope.launch {
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

                viewLifecycleOwner.lifecycleScope.launch {
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
}