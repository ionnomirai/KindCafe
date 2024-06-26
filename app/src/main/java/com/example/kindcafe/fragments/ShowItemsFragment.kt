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
import androidx.navigation.fragment.navArgs
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
import com.example.kindcafe.databinding.FragItemsBinding
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.firebase.StorageManager
import com.example.kindcafe.firebase.firebaseEnums.UriSize
import com.example.kindcafe.firebase.firebaseInterfaces.GetUrisCallback
import com.example.kindcafe.firebase.firebaseInterfaces.ReadAndSplitCategories
import com.example.kindcafe.utils.AuxillaryFunctions
import com.example.kindcafe.utils.Locations
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ShowItemsFragment : Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragItemsBinding? = null
    private val binding
        get() : FragItemsBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    /* Common viewModel between activity and this fragment */
    private val mainVM: MainViewModel by activityViewModels()
    private lateinit var mainActivity: MainActivity

    //private val myAdapter = AdapterShowItems(clickItemElements())
    private lateinit var myAdapter: AdapterShowItems

    private val my_tag = "ShowItemsFragment"
    private val navArgs: ShowItemsFragmentArgs by navArgs()

    private val dbManager = DbManager()
    private val storageManager = StorageManager()

    val dList = mutableListOf<Dish>()
    val uSmallList = mutableListOf<Dish>()
    val uBigList = mutableListOf<Dish>()

    val goForwardMainData = MutableStateFlow(false)
    val goForwardUriSmallData = MutableStateFlow(false)
    val goForwardUriBigData = MutableStateFlow(false)

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragItemsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myAdapter = AdapterShowItems(
            AuxillaryFunctions.deafultItemMoveDirections(
                this,
                mainVM,
                { dish: Dish ->
                    ShowItemsFragmentDirections.actionShowItemsFragmentToDetailFragment(
                        dish.id,
                        dish.name!!
                    )
                }
            )
        )

        mainActivity = activity as MainActivity

        binding.rvShowItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myAdapter
        }

        // retrieve data from Local DB
        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.getDishesByCategory(navArgs.category)
        }

        // try to read data from Realtime DB
        dbManager.readDishDataFromDb(
            navArgs.category,
            clarificationGetDataFirebase()
        )

        // First try if we retrieve data from RDB, then retrieve uriSmall
        viewLifecycleOwner.lifecycleScope.launch {
            goForwardMainData.collect {
                if (it) {
                    Log.d(my_tag, "data added: Uri small")
                    storageManager.readUri(
                        dList,
                        clarificationGetUris(UriSize.Small),
                        UriSize.Small
                    )
                }
            }
        }

        // Next retrieve uriBig
        viewLifecycleOwner.lifecycleScope.launch {
            goForwardUriSmallData.collect {
                if (it) {
                    Log.d(my_tag, "data added: : Uri big")
                    storageManager.readUri(
                        uSmallList,
                        clarificationGetUris(UriSize.Big),
                        UriSize.Big
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            goForwardUriBigData.collect { third ->
                if (third) {
                    mainVM.addDishLocal(uBigList)
                    Log.d(my_tag, "all added")
                    goForwardMainData.value = false
                    goForwardUriSmallData.value = false
                    goForwardUriBigData.value = false
                    // we need get out there "Wait sign"
                }
            }
        }

        // Data we pass to adapter
        viewLifecycleOwner.lifecycleScope.launch {
            checkCategoryAndAction(navArgs.category)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.needUpdate.collect {
                if (it) {
                    myAdapter.notifyDataSetChanged()
                    mainVM.needUpdate.value = false
                }
            }
        }

    }

    private suspend fun checkCategoryAndAction(categoryCur: Categories) {
        when (categoryCur) {
            Categories.SparklingDrinks -> mainVM.sparklingDrinks.collect {
                myAdapter.setNewData(it)
                mainVM.currentLocation = Locations.SHOW_SPARKLING_DRINKS.nameL
            }
            Categories.NonSparklingDrinks -> mainVM.nonSparklingDrinks.collect{
                myAdapter.setNewData(it)
                mainVM.currentLocation = Locations.SHOW_NON_SPARKLING_DRINKS.nameL
            }
            Categories.Sweets -> mainVM.sweets.collect{
                myAdapter.setNewData(it)
                mainVM.currentLocation = Locations.SHOW_SWEETS.nameL
            }
            Categories.Cakes -> mainVM.cakes.collect {
                myAdapter.setNewData(it)
                mainVM.currentLocation = Locations.SHOW_CAKES.nameL
            }
        }
    }

    private fun clarificationGetDataFirebase(): ReadAndSplitCategories {
        return object : ReadAndSplitCategories {
            override fun readAndSplit(data: List<Dish>) {
                dList.clear()
                dList += data
                goForwardMainData.value = true
            }
        }
    }

    private fun clarificationGetUris(uriSize: UriSize): GetUrisCallback {
        return object : GetUrisCallback {
            override fun getUris(newData: List<Dish>) {
                if (uriSize == UriSize.Small) {
                    uSmallList.clear()
                    uSmallList += newData
                    goForwardUriSmallData.value = true
                } else {
                    uBigList.clear()
                    uBigList += newData
                    goForwardUriBigData.value = true
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(my_tag, "onResume")
        mainActivity.everyOpenHomeSettings()
        mainActivity.supportActionBar?.title = ""
        mainActivity.binding.tvToolbarTitle.text = navArgs.category.categoryName
    }

    override fun onPause() {
        super.onPause()
        Log.d(my_tag, "onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(my_tag, "onDestroy")
    }
}