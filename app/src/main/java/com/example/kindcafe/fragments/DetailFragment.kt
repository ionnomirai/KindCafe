package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.kindcafe.databinding.FragDishDetailBinding
import com.example.kindcafe.viewModels.MainViewModel
import androidx.navigation.fragment.navArgs
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.database.Favorites
import com.example.kindcafe.database.OrderItem
import com.example.kindcafe.utils.AuxillaryFunctions
import com.example.kindcafe.utils.GeneralAccessTypes
import com.squareup.picasso.Picasso
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class DetailFragment : Fragment() {
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
    private var isFavorite = false
    private var isInBasket = false

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

        // get data from nav args
        val idDish = navArgs.id
        val nameDish = navArgs.name
        val colorActive = AppCompatResources.getColorStateList(requireContext(), R.color.greeting_phrase_color)
        val colorNonActive = AppCompatResources.getColorStateList(requireContext(), R.color.item_icon)
        val checkDishInterface = AuxillaryFunctions.defaultCheckFavBasketInterface()

        val mainActivity = activity as MainActivity
        mainActivity.supportActionBar?.title = ""
        mainActivity.accessUpperPart(GeneralAccessTypes.CLOSE)  // close upper part (tb title and icons)
        mainActivity.accessBottomPart(GeneralAccessTypes.CLOSE) // close bottom part

        // find dish
        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.getDish(idDish, nameDish)
            cancel()
        }

        // register changes in favorites
        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.favorites.collect { favorites ->
                isFavorite = favorites
                    .filter { it.id == idDish && it.dishId == idDish && it.dishName == nameDish }
                    .isNotEmpty()
                checkDishInterface.checkAndFillFavorites(
                    isFavorite, binding.ibFavorite, colorActive, colorNonActive
                )
            }
        }

        // register changes in bag
        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.orderBasket.collect { basket ->
                isInBasket = basket.filter {it.id == idDish && it.name == nameDish }.isNotEmpty()
                checkDishInterface.checkAndFillBasket(
                    isInBasket, binding.ibToBag, colorActive, colorNonActive
                )
            }
        }

        binding.apply {
            ibFavorite.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    checkDishInterface.putOrDelFavorite(
                        isFavorite, mainVM, idDish, nameDish
                    )
                    cancel()
                }
            }

            ibToBag.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch{
                    checkDishInterface.putOrDelBasket(
                        isInBasket, mainVM, idDish, nameDish
                    )
                    cancel()
                }
            }
        }

        // common setting field
        lifecycleScope.launch {
            mainVM.currentDish.collect { dish ->
                binding.apply {
                    Picasso.get().load(dish.uriBig).into(ivPicBig)
                    tvNameDish.text = dish.name
                    tvPriceDish.text = resources.getString(R.string.price_style_usd, dish.price)
                    tvDescription.text = dish.description
                }
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