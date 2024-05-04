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
import com.example.kindcafe.adapters.AdapterShowItems
import com.example.kindcafe.adapters.callbacks.ItemMoveDirections
import com.example.kindcafe.data.Categories
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.Favorites
import com.example.kindcafe.databinding.FragBasketBinding
import com.example.kindcafe.databinding.FragHomeBinding
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BasketFrag: Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragBasketBinding? = null
    private val binding
        get() : FragBasketBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    /* Common viewModel between activity and this fragment */
    private val mainVM: MainViewModel by activityViewModels()

    private val my_tag = "BasketFragmentTag"

    private val myAdapter = AdapterShowItems(clickItemElements())

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragBasketBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvItemsBuy.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.allDishes.collect{
                myAdapter.setNewData(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(my_tag, "onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    fun clickItemElements(): ItemMoveDirections{
        return object : ItemMoveDirections{
            override fun detailed(dish: Dish) {
                TODO("Not yet implemented")
            }

            override fun putToBag(dish: Dish) {
                TODO("Not yet implemented")
            }

            override fun putToFavorite(favoriteDish: Favorites) {
                TODO("Not yet implemented")
            }

            override fun delFromFavorite(favoriteDish: Favorites) {
                TODO("Not yet implemented")
            }

            override fun checkFavorites(favoriteDish: Favorites): Boolean {
                TODO("Not yet implemented")
            }
        }
    }
}