package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kindcafe.MainActivity
import com.example.kindcafe.adapters.AdapterShowItems
import com.example.kindcafe.databinding.FragHomeBinding
import com.example.kindcafe.databinding.FragItemsBinding
import com.example.kindcafe.viewModels.MainViewModel

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
        myAdapter.setNewData(mainVM.sparklingDrinks)
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