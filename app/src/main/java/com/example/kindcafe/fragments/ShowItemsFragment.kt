package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.adapters.AdapterShowItems
import com.example.kindcafe.data.Categories
import com.example.kindcafe.database.Dish
import com.example.kindcafe.databinding.FragHomeBinding
import com.example.kindcafe.databinding.FragItemsBinding
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.firebase.firebaseInterfaces.ReadAndSplitCategories
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ShowItemsFragment: Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragItemsBinding? = null
    private val binding
        get() : FragItemsBinding {
            return checkNotNull(_binding){
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    /* Common viewModel between activity and this fragment */
    private val mainVM : MainViewModel by activityViewModels()
    private val myAdapter = AdapterShowItems()

    private val homeFragmentTag = "ShowItemsFragment"

    private val dbManager = DbManager()

    val dList = mutableListOf<Dish>()

    val goForward = MutableStateFlow(false)



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

        val mainActivity = activity as MainActivity
        mainActivity.supportActionBar?.title = ""
        mainActivity.binding.tvToolbarTitle.text = "Sparkling drinks"

        binding.rvShowItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myAdapter
        }
        /* Look, now it is only for sparkling - it is test */

        dbManager.readDishDataFromDb(
            Categories.SparklingDrinks,
            clarificationGetDataFirebase()
        )
        viewLifecycleOwner.lifecycleScope.launch {
            goForward.collect{
                if (it){
                    mainVM.addDishLocal(dList)
                    goForward.value = false
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.sparklingDrinks.collect{
                myAdapter.setNewData(it)
            }
        }


    }

    private fun clarificationGetDataFirebase(): ReadAndSplitCategories {
        return object : ReadAndSplitCategories {
            override fun readAndSplit(data: List<Dish>) {
                dList.clear()
                dList += data
                goForward.value = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(homeFragmentTag, "onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}