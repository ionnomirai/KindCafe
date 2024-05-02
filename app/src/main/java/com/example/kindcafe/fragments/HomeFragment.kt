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
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.data.Categories
import com.example.kindcafe.database.Dish
import com.example.kindcafe.databinding.FragHomeBinding
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.firebase.firebaseInterfaces.ReadAndSplitCategories
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragHomeBinding? = null
    private val binding
        get() : FragHomeBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    /* Common viewModel between activity and this fragment */
    private val mainVM: MainViewModel by activityViewModels()

    private val myTag = "HomeFragmentTag"

/*    private val dbManager = DbManager()

    val dList = mutableListOf<Dish>()

    val goForward = MutableStateFlow(false)*/

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).everyOpenHomeSettings()

        Log.d(myTag, "onViewCreated")

        mainVM.nameData.observe(viewLifecycleOwner) { email ->
            binding.tvUserNameHome.text = email
        }

        binding.apply {
            cvFirst.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToShowItemsFragment(Categories.SparklingDrinks)
                findNavController().navigate(action)
            }
            cvFourth.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToShowItemsFragment(Categories.Cakes)
                findNavController().navigate(action)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d(myTag, "onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}