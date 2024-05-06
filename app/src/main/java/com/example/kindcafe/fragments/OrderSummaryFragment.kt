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
import com.example.kindcafe.databinding.FragOrderSummaryBinding
import com.example.kindcafe.viewModels.MainViewModel

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
        (activity as MainActivity).everyOpenHomeSettings()

        Log.d(my_tag, "onViewCreated")

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