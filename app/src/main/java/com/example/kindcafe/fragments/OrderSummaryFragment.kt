package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.kindcafe.data.CakesAdditive
import com.example.kindcafe.data.Categories
import com.example.kindcafe.data.DetailedOrderItem
import com.example.kindcafe.data.NonSparklingAdditive
import com.example.kindcafe.data.Size
import com.example.kindcafe.data.SparklingDrinksAdditive
import com.example.kindcafe.data.SweetsAdditive
import com.example.kindcafe.databinding.FragOrderSummaryBinding
import com.example.kindcafe.viewModels.MainViewModel
import kotlin.math.log

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

    private val my_tag = "OrderSummaryFragmentTag"
    private val myArgs: OrderSummaryFragmentArgs by navArgs()

    /* if the args not null, save all data there */
    private val listDetailed: MutableList<DetailedOrderItem> = mutableListOf()

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

        myArgs.detailedOrder?.let {
            listDetailed.addAll(it.toList())
            Log.d(my_tag, listDetailed.toString())
        }

        binding.tvBill.text = formBill(listDetailed)
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
            val tempResult = checkAdditiveAndSize(it, mapAdds) // need to split to result and totalPrice
            result
                .append( tempResult.first ) // create bill_item for one dish
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
            Size.Small.name -> Size.Small.coefficient
            Size.Medium.name -> Size.Medium.coefficient
            Size.Big.name -> Size.Big.coefficient
            else -> 1.0
        }

        val resultText =
            StringBuilder().append("${item.name}, ${item.count} item(s) (${item.price}$), ${item.size} size(*$sizeLikePrice),")
        val resultPriceText = StringBuilder("\n= (${item.price}$ * $sizeLikePrice + ")
        var resultPrice = 0.0

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