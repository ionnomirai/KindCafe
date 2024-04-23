package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.databinding.FragHomeBinding
import com.example.kindcafe.viewModels.MainViewModel

class HomeFragment: Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragHomeBinding? = null
    private val binding
        get() : FragHomeBinding{
            return checkNotNull(_binding){
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    /* Common viewModel between activity and this fragment */
    private val mainVM : MainViewModel by activityViewModels()

    private val homeFragmentTag = "HomeFragmentTag"

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

        Log.d(homeFragmentTag, "onViewCreated")

        mainVM.nameData.observe(viewLifecycleOwner) {email ->
            binding.tvUserNameHome.text = email
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