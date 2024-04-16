package com.example.kindcafe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kindcafe.MainActivity
import com.example.kindcafe.databinding.FragHomeBinding

class HomeFragment: Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragHomeBinding? = null
    private val binding
        get() : FragHomeBinding{
            return checkNotNull(_binding){
                "Cannot access binding because it is null. Is the view visible"
            }
        }


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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}