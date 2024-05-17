package com.example.kindcafe.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.R
import com.example.kindcafe.adapters.AdapterBasket
import com.example.kindcafe.adapters.AdapterShowItems
import com.example.kindcafe.data.DetailedOrderItem
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.OrderItem
import com.example.kindcafe.databinding.DialogSearchGeneralBinding
import com.example.kindcafe.interfaces.SwipeBasketItem
import com.example.kindcafe.utils.AuxillaryFunctions
import com.example.kindcafe.utils.Locations
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale


class DialogSearchGeneral(private val location: String) : DialogFragment() {
    private val my_tag = "DialogFragmentTag"

    //private lateinit var binding: DialogSearchGeneralBinding
    private var _binding: DialogSearchGeneralBinding? = null
    private val binding
        get(): DialogSearchGeneralBinding {
            return checkNotNull(_binding){
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    private val mainViewModel : MainViewModel by activityViewModels()
    private lateinit var myAdapterShowItems : AdapterShowItems
    private var myAdapterBasket: AdapterBasket? = null

    private val currentListDishes = mutableListOf<Dish>()

    private var jobBasket: Job? = null
    private var jobBasketDelPos: Job? = null

    private var swipeBackground: ColorDrawable = ColorDrawable(Color.parseColor("#FF0000"))
    private lateinit var deleteIcon: Drawable
    private lateinit var itemTouchHelper: ItemTouchHelper
    private val filterList = mutableListOf<DetailedOrderItem>()
    private val flowDelPosition = MutableStateFlow(-1)

    val tempList1 = mutableListOf<DetailedOrderItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogSearchGeneralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.needUpdate.value = false

        binding.rvSearchResult.apply {
            layoutManager = LinearLayoutManager(requireContext())

        }

        /* creating adapter */
        if (location == Locations.BASKET.nameL){
            myAdapterBasket = AdapterBasket(
                AuxillaryFunctions.deafultItemMoveDirections(this, mainViewModel, null),
                AuxillaryFunctions.defaultClickSettingOrder(this, mainViewModel)
            )
            binding.rvSearchResult.adapter = myAdapterBasket

            val obj1 = object : SwipeBasketItem{
                override fun getDelPosition(position: Int) {
                    flowDelPosition.value = position
                }
            }

            deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)!!
            itemTouchHelper = ItemTouchHelper(AuxillaryFunctions.defaultSwipeDelBasketOrder(
                deleteIcon, swipeBackground, swipeBasketItem = obj1
            ))
            itemTouchHelper.attachToRecyclerView(binding.rvSearchResult)

        } else {
            myAdapterShowItems = AdapterShowItems(
                AuxillaryFunctions.deafultItemMoveDirections(this, mainViewModel,null)
            )
            binding.rvSearchResult.adapter = myAdapterShowItems
            setSearchViewListener(myAdapterShowItems, binding.svSearch, currentListDishes)
        }

        // current location - set title (it is need to create fun and correct output)
        binding.tvTitleDialog.text = location
        when(location){
            Locations.FAVORITES.nameL -> currentListDishes.addAll( mainViewModel.favoritesLikeDish.value )
            Locations.BASKET.nameL -> {
                jobBasket = viewLifecycleOwner.lifecycleScope.launch {
                    mainViewModel.orderBasket.collect{
                        Log.d(my_tag, "order basket collect")
                        myAdapterBasket?.notifyDataSetChanged()
                        val detailedList1 = AuxillaryFunctions.transformOrdItemToDishesDetailed(
                            it, mainViewModel.allDishes.value
                        )

                        filterList.clear()
                        detailedList1.forEach {dl1 ->
                            tempList1.forEach {tl1 ->
                                if(tl1.id == dl1.id){
                                    filterList.add(dl1)
                                }
                            }
                        }

                        myAdapterBasket?.setNewData(filterList)

                        setSearchViewListener(
                            searchView = binding.svSearch,
                            adapterBasket = myAdapterBasket,
                            listBasket = detailedList1)
                    }
                }

                jobBasketDelPos =this@DialogSearchGeneral.viewLifecycleOwner.lifecycleScope.launch {
                    flowDelPosition.collect{
                        if(it>=0){
                            Log.d(my_tag, "delPos: $it, current data size: ${myAdapterBasket?.getCurrentList()?.size}")
                            val adapterList = myAdapterBasket?.getCurrentList() ?: emptyList()
                            if (it < adapterList.size){
                                val item = OrderItem(
                                    id = adapterList[it].id,
                                    name = adapterList[it].name,
                                    add1 = adapterList[it].add1,
                                    add2 = adapterList[it].add2,
                                    add3 = adapterList[it].add3,
                                    size = adapterList[it].size,
                                    count = adapterList[it].count
                                )
                                mainViewModel.deleteOrderItemsLocal(item)
                                mainViewModel.dbManager.deleteOrderBasketItemFromRDB(
                                    KindCafeApplication.myAuth.currentUser,
                                    item
                                )
                                flowDelPosition.value = -1
                            }

                        }
                        Log.d(my_tag, "current del position = $it")
                    }
                }
            }
            Locations.SHOW_SPARKLING_DRINKS.nameL -> currentListDishes.addAll( mainViewModel.sparklingDrinks.value )
            Locations.SHOW_NON_SPARKLING_DRINKS.nameL -> ""
            Locations.SHOW_SWEETS.nameL -> ""
            Locations.SHOW_CAKES.nameL -> currentListDishes.addAll( mainViewModel.cakes.value )
            else ->{ currentListDishes.addAll( mainViewModel.allDishes.value ) }
        }

        binding.apply{
            ibCloseDialog.setOnClickListener {
                this@DialogSearchGeneral.dismiss()
            }

        }
    }

    private fun setSearchViewListener(
        adapter: AdapterShowItems? = null,
        searchView: SearchView? = null,
        list: List<Dish>? = null,
        adapterBasket: AdapterBasket? = null,
        listBasket: List<DetailedOrderItem>? = null,
    ){
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean { return false }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(listBasket == null){
                    val tempList =  list?.let { filterListDataShow(it, newText)} ?: emptyList()
                    adapter?.setNewData(tempList)
                } else {
                    val tempList =  filterListDataBasket(listBasket, newText)
                    tempList1.clear()
                    tempList1.addAll(tempList)
                    adapterBasket?.setNewData(tempList)
                }

                return true
            }
        })
    }

    private fun filterListDataShow(listDishes: List<Dish>, searchText: String?) : List<Dish>{
        val tempList = mutableListOf<Dish>()
        tempList.clear()

        if(searchText != null){
            listDishes.forEach {dish ->
                dish.name?.let { dishName ->
                    if(dishName.lowercase(Locale.ROOT).startsWith(searchText.lowercase(Locale.ROOT))
                        && !searchText.isBlank()){
                        tempList.add(dish)
                    }
                }
            }
        }

        return tempList
    }

    private fun filterListDataBasket(listDishesBasket: List<DetailedOrderItem>, searchText: String?) : List<DetailedOrderItem>{
        val tempList = mutableListOf<DetailedOrderItem>()
        tempList.clear()

        if(searchText != null){
            listDishesBasket.forEach {dish ->
                dish.name?.let { dishName ->
                    if(dishName.lowercase(Locale.ROOT).startsWith(searchText.lowercase(Locale.ROOT))
                        && !searchText.isBlank()){
                        tempList.add(dish)
                    }
                }
            }
        }
        return tempList
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            //Log.d(my_tag, "w: $width, h: $height")
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        jobBasket = null
        jobBasketDelPos = null
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.needUpdate.value = true
        Log.d(my_tag, "dialog -- OnDestroy")
    }
}