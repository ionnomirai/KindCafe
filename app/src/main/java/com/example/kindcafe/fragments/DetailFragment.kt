package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.kindcafe.databinding.FragDishDetailBinding
import com.example.kindcafe.viewModels.MainViewModel
import androidx.navigation.fragment.navArgs
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.utils.GeneralAccessTypes
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class DetailFragment: Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragDishDetailBinding? = null
    private val binding
        get() : FragDishDetailBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    private val myTag = "FragDishDetailTag"

    /* Common viewModel between activity and this fragment */
    private val mainVM: MainViewModel by activityViewModels()

    private val navArgs: DetailFragmentArgs by navArgs()

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragDishDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idDish = navArgs.id
        val nameDish = navArgs.name

        val mainActivity = activity as MainActivity
        mainActivity.supportActionBar?.title = ""
        mainActivity.accessUpperPart(GeneralAccessTypes.CLOSE)  // close upper part (tb title and icons)
        mainActivity.accessBottomPart(GeneralAccessTypes.CLOSE) // close bottom part

        lifecycleScope.launch {
            mainVM.getDish(idDish, nameDish)
        }

        lifecycleScope.launch {
            mainVM.currentDish.collect{dish ->
                binding.apply {
                    Picasso.get().load(dish.uriBig).into(ivPicBig)
                    tvNameDish.text = dish.name
                    tvPriceDish.text = resources.getString(R.string.price_style, dish.price)
                    tvDescription.text = dish.description
                }
            }
        }



        Log.d(myTag, "onViewCreated")
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