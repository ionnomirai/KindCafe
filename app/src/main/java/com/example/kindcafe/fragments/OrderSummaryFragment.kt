package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.data.CakesAdditive
import com.example.kindcafe.data.Categories
import com.example.kindcafe.data.DetailedOrderItem
import com.example.kindcafe.data.NonSparklingAdditive
import com.example.kindcafe.data.Size
import com.example.kindcafe.data.SparklingDrinksAdditive
import com.example.kindcafe.data.SweetsAdditive
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.OrderItemPlaced
import com.example.kindcafe.databinding.FragOrderSummaryBinding
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.utils.AuxillaryFunctions
import com.example.kindcafe.utils.SimplePopDirections
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OrderSummaryFragment : Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragOrderSummaryBinding? = null
    private val binding
        get() : FragOrderSummaryBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }



    /* Common viewModel between activity and this fragment */
    private val mainVM: MainViewModel by activityViewModels()
    private val dbManager = DbManager()

    private val my_tag = "OrderSummaryFragmentTag"
    private val currentFragmentName = "OrderSummary"

    //private val myNavArgs: OrderSummaryFragmentArgs by navArgs()
    //private var myNavArgs: Array<DetailedOrderItem>? = null
    private var argsDetailedOrderArray: Array<DetailedOrderItem>? = null

    /* if the args not null, save all data there */
    private val listDetailed: MutableList<DetailedOrderItem> = mutableListOf()
    private var orderPlaced: List<OrderItemPlaced>? = null

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragOrderSummaryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(my_tag, "onViewCreated")

        val mainAct = activity as? MainActivity

        mainAct?.let {
            it.binding.tbMain.menu.findItem(R.id.itbSearch).isVisible = false
            it.binding.tvToolbarTitle.text = currentFragmentName
            it.supportActionBar?.title = ""
        }

        try {
            arguments?.let {
                argsDetailedOrderArray = OrderSummaryFragmentArgs.fromBundle(it).detailedOrder
            }
        } catch (e: Exception){
            argsDetailedOrderArray = null
            Log.d(my_tag, "exception = ${e.message}")
        }

        Log.d(my_tag, "argsDetailedOrderArray = ${argsDetailedOrderArray}")
        argsDetailedOrderArray?.let {
            listDetailed.addAll(it.toList())
            Log.d(my_tag, listDetailed.toString())
            binding.tvBill.text = formBill(listDetailed)

            orderPlaced = List(listDetailed.size) { index ->
                OrderItemPlaced(
                    id = listDetailed[index].id,
                    name = listDetailed[index].name,
                    add1 = listDetailed[index].add1,
                    add2 = listDetailed[index].add2,
                    add3 = listDetailed[index].add3,
                    size = listDetailed[index].size,
                    count = listDetailed[index].count
                )
            }
            //Log.d(my_tag, "orderPlaced: ${orderPlaced.toString()}")
        }

        // if nullm then we move here from Home and order has already placed
        if(argsDetailedOrderArray == null){
            val tempDishes = mutableListOf<Dish>()

            mainVM.orderPlaced.value.forEach { placed ->
                Log.d(my_tag, "placed ${placed}")
                Log.d(my_tag, "allDishes ${mainVM.allDishes.value}")
                val dish = mainVM.allDishes.value.find{(it.id == placed.id && it.name == placed.name)}
                Log.d(my_tag, "dish ${dish}")
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
                    add1 = mainVM.orderPlaced.value[index].add1,
                    add2 = mainVM.orderPlaced.value[index].add2,
                    add3 = mainVM.orderPlaced.value[index].add3,
                    size = mainVM.orderPlaced.value[index].size,
                    count = mainVM.orderPlaced.value[index].count
                )
            }


            binding.apply {
                tvSend.visibility = View.GONE
                tvReject.visibility = View.VISIBLE
                tvBill.text = formBill(detailedList)
            }

        }



        binding.tvSend.setOnClickListener {
            // if we want to place order to server
            orderPlaced?.let {listItemsPlaced ->
                mainVM.viewModelScope.launch {
                    listItemsPlaced.forEach { item ->
                        mainVM.addOrderPlacedLocal(item)
                    }
                    mainVM.deleteAllOrderItemsLocal()
                    dbManager.deleteOrderBasketFromRDB(KindCafeApplication.myAuth.currentUser)
                    dbManager.setOrderPlacedToRDB(
                        user = KindCafeApplication.myAuth.currentUser,
                        data = listItemsPlaced,
                        defStatus = AuxillaryFunctions.defaultDefinitionOfStatusInterface(
                            this@OrderSummaryFragment,
                            SimplePopDirections.TOP_DESTINATION
                        )
                    )
                    cancel()
                }
            }
        }

        binding.tvReject.setOnClickListener {
            mainVM.viewModelScope.launch {
                mainVM.deleteAllOrderPlacedLocal()
                cancel()
            }
            dbManager.deleteOrderPlacedFromRDB(
                KindCafeApplication.myAuth.currentUser,
                AuxillaryFunctions.defaultDefinitionOfStatusInterface(
                    this@OrderSummaryFragment,
                    SimplePopDirections.TOP_DESTINATION
                ))
        }
    }

    private fun formBill(data: List<DetailedOrderItem>): String {
        val result = StringBuilder()
        var totalPrice = 0.0
        val mapAdds = mutableListOf<Pair<String, Double>>()
        data.forEach {
            mapAdds.clear() // it needs because every dish may have different additives
            when (it.category) {
                Categories.SparklingDrinks.categoryName -> {
                    // collect information about additional names and prices
                    SparklingDrinksAdditive.entries.forEach { mapAdds.add(Pair(it.addsName, it.addsPrice)) }
                }

                Categories.NonSparklingDrinks.categoryName -> {
                    NonSparklingAdditive.entries.forEach { mapAdds.add(Pair(it.addsName, it.addsPrice)) }
                }

                Categories.Sweets.categoryName -> {
                    SweetsAdditive.entries.forEach { mapAdds.add(Pair(it.addsName, it.addsPrice)) }
                }

                Categories.Cakes.categoryName -> {
                    CakesAdditive.entries.forEach { mapAdds.add(Pair(it.addsName, it.addsPrice)) }
                }
            }
            val tempResult =
                checkAdditiveAndSize(it, mapAdds) // need to split to result and totalPrice
            result
                .append(tempResult.first) // create bill_item for one dish
                .append("\n\n")
            totalPrice += tempResult.second
        }
        return result.append("\n\nTotal price: ${String.format("%.2f$", totalPrice)}").toString()
    }

    private fun checkAdditiveAndSize(
        item: DetailedOrderItem,
        priceList: List<Pair<String, Double>>
    ): Pair<String, Double> {
        // depending on the size, set the coefficient.
        val sizeLikePrice = when (item.size) {
            Size.Small.name, null -> Size.Small.coefficient
            Size.Medium.name -> Size.Medium.coefficient
            Size.Big.name -> Size.Big.coefficient
            else -> 1.0
        }

        // Creates a corresponding list of additives and their prices for a given dish
        val adds: List<Pair<String, Double>> =
            listOf(item.add1, item.add2, item.add3)
                .mapIndexed() { index, b ->
                    if ((b != null && b))
                        priceList[index]
                    else
                        Pair("", -1.0)
                }
                .filter { it.first.isNotEmpty() }

        val resultText =
            StringBuilder()
                .append("${item.name}, ${item.count} item(s) " +
                        "(${item.price}$), ${item.size ?: "Small"} size(*$sizeLikePrice)")
        val resultPriceText = StringBuilder("\n= (${item.price}$ * $sizeLikePrice")

        if (adds.isNotEmpty()) {
            resultText.append(",")
            resultPriceText.append(" + ")
        } else {
            resultPriceText.append(") ")
        }


        var resultPrice = 0.0



        if (item.price?.toDoubleOrNull() != null && item.count?.toDoubleOrNull() != null) {
            resultPrice = sizeLikePrice * item.price.toDouble()

            adds.forEachIndexed { index, pair ->
                resultText.append(" +${pair.first} (+${pair.second}$)")
                resultPriceText.append("${pair.second}$")
                resultPrice += pair.second
                if (index != adds.lastIndex) {
                    resultText.append(",")
                    resultPriceText.append(" + ")
                } else {
                    resultPriceText.append(") * ${item.count}")
                    resultPrice *= item.count.toDouble()
                }
            }
        }
        resultText.append(resultPriceText).append(String.format(" = %.2f$", resultPrice))
        return Pair(resultText.toString(), resultPrice)
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