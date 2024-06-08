package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.kindcafe.MainActivity
import com.example.kindcafe.data.Categories
import com.example.kindcafe.databinding.FragHomeBinding
import com.example.kindcafe.utils.Locations
import com.example.kindcafe.viewModels.MainViewModel


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

    private val my_tag = "HomeFragmentTag"

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

        Log.d(my_tag, "onViewCreated")

        mainVM.nameData.observe(viewLifecycleOwner) { email ->
            binding.tvUserNameHome.text = email
        }

        binding.apply {
            try {
                cvFirst.setOnClickListener {
                    val action = HomeFragmentDirections.actionHomeFragmentToShowItemsFragment(Categories.SparklingDrinks)
                    findNavController().navigate(action)
                }
                cvSecond.setOnClickListener {
                    val action = HomeFragmentDirections.actionHomeFragmentToShowItemsFragment(Categories.NonSparklingDrinks)
                    findNavController().navigate(action)
                }
                cvThird.setOnClickListener {
                    val action = HomeFragmentDirections.actionHomeFragmentToShowItemsFragment(Categories.Sweets)
                    findNavController().navigate(action)
                }
                cvFourth.setOnClickListener {
                    val action = HomeFragmentDirections.actionHomeFragmentToShowItemsFragment(Categories.Cakes)
                    findNavController().navigate(action)
                }
            } catch (e: Exception){
                Log.d(my_tag, "Navigation exception: $e")
            }

        }

    }

    override fun onResume() {
        super.onResume()
        Log.d(my_tag, "onResume")
        mainVM.currentLocation = Locations.HOME.nameL
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}