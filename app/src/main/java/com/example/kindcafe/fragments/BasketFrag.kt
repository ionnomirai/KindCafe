package com.example.kindcafe.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.kindcafe.utils.AuxillaryFunctions
import com.example.kindcafe.utils.Locations
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
    private val currentFragmentName = "Basket"

    //private val myAdapter = AdapterBasket(clickItemElements(), clickSettingOrder())
    private lateinit var myAdapter : AdapterBasket
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

        myAdapter = AdapterBasket(
            AuxillaryFunctions.deafultItemMoveDirections(this, mainVM, null),
            AuxillaryFunctions.defaultClickSettingOrder(this, mainVM)
        )

        mainVM.currentLocation
        val mainAct = activity as? MainActivity

        mainAct?.let {
            it.binding.tvToolbarTitle.text = currentFragmentName
            it.supportActionBar?.title = ""
        }

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
                dbManager.setOrderBasketToRDB(KindCafeApplication.myAuth.currentUser, it)

/*                tempDishes.clear()
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
                detailedListI.addAll(detailedList)*/

                val detailedList = AuxillaryFunctions.transformOrdItemToDishesDetailed(
                    it, mainVM.allDishes.value
                )
                detailedListI.clear()
                detailedListI.addAll(detailedList)

                Log.d(my_tag, "size detailed list: ${detailedList.size}")
                Log.d(my_tag, "list: ${detailedList}")
                myAdapter.setNewData(detailedList)
            }
        }

        binding.clButtonMakeOrder.setOnClickListener {

            val someDishesHaveNotQuantity = detailedListI
                .filter { it.count == "0" || it.count == null}
                .isNotEmpty()
            /* if all dishes have quantity (count), then we can move further */
            if (!someDishesHaveNotQuantity){
                val action = BasketFragDirections.actionBasketFragToOrderSummaryFragment(detailedListI.toTypedArray())
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, R.string.quatity_zero, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainVM.currentLocation = Locations.BASKET.nameL
        Log.d(my_tag, "onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}