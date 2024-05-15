package com.example.kindcafe.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kindcafe.adapters.AdapterShowItems
import com.example.kindcafe.data.Categories
import com.example.kindcafe.database.Dish
import com.example.kindcafe.databinding.DialogSearchGeneralBinding
import com.example.kindcafe.utils.AuxillaryFunctions
import com.example.kindcafe.utils.Locations
import com.example.kindcafe.viewModels.MainViewModel
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

    private val currentListDishes = mutableListOf<Dish>()

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

        myAdapterShowItems = AdapterShowItems(
            AuxillaryFunctions.deafultItemMoveDirections(this, mainViewModel,null)
        )

        binding.rvSearchResult.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myAdapterShowItems
        }

        // current location - set title (it is need to create fun and correct output)
        binding.tvTitleDialog.text = location
        when(location){
            Locations.FAVORITES.nameL -> currentListDishes.addAll( mainViewModel.favoritesLikeDish.value )
            Locations.BASKET.nameL -> ""
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

            tvTitleDialog.setOnClickListener {
                DialogSearchGeneral(Locations.HOME.nameL).show(parentFragmentManager,null)
            }

            setSearchViewListener(myAdapterShowItems, currentListDishes, svSearch)

        }
    }

    private fun setSearchViewListener(
        adapter: AdapterShowItems,
        list: List<Dish>,
        searchView: SearchView?
    ){
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean { return false }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempList = filterListData(list, newText)
                Log.d(my_tag, "search: $newText size: ${newText?.length}, |||| templist ${tempList}")
                adapter.setNewData(tempList)
                return true
            }
        })
    }

    private fun filterListData(listDishes: List<Dish>, searchText: String?) : List<Dish>{
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
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.needUpdate.value = true
        Log.d(my_tag, "dialog -- OnDestroy")
    }
}