package com.example.kindcafe.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.kindcafe.R
import com.example.kindcafe.database.Dish
import com.example.kindcafe.databinding.FragDishDetailBinding
import com.example.kindcafe.interfaces.ActionDialogDetailedDismiss
import com.example.kindcafe.utils.AuxillaryFunctions
import com.example.kindcafe.viewModels.MainViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DialogDetailedDish(
    private val dish: Dish,
    private val dismissInterface: ActionDialogDetailedDismiss? = null
): DialogFragment() {
    private val my_tag = "DialogFragmentTag"

    private var _binding: FragDishDetailBinding? = null
    private val binding
        get(): FragDishDetailBinding {
            return checkNotNull(_binding){
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    private var picassoJob: Job? = null
    private val mainVM: MainViewModel by activityViewModels()
    private var isFavorite = false
    private var isInBasket = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragDishDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorActive = AppCompatResources.getColorStateList(requireContext(), R.color.greeting_phrase_color)
        val colorNonActive = AppCompatResources.getColorStateList(requireContext(), R.color.item_icon)
        val checkDishInterface = AuxillaryFunctions.defaultCheckFavBasketInterface()

        // register changes in favorites
        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.favorites.collect { favorites ->
                isFavorite = favorites
                    .filter { it.id == dish.id && it.dishId == dish.id && it.dishName == dish.name }
                    .isNotEmpty()
                checkDishInterface.checkAndFillFavorites(
                    isFavorite, binding.ibFavorite, colorActive, colorNonActive
                )
            }
        }

        // register changes in bag
        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.orderBasket.collect { basket ->
                isInBasket = basket.filter {it.id == dish.id && it.name == dish.name }.isNotEmpty()
                checkDishInterface.checkAndFillBasket(
                    isInBasket, binding.ibToBag, colorActive, colorNonActive
                )
            }
        }

        binding.apply {
            ibCloseDialogD.visibility = View.VISIBLE
            ibCloseDialogD.setOnClickListener {
                dismissInterface?.actionWhenDismiss()
                this@DialogDetailedDish.dismiss()
            }

            picassoJob = this@DialogDetailedDish.lifecycleScope.launch {
                Picasso.get().load(dish.uriBig).into(ivPicBig)
            }

            tvNameDish.text = dish.name
            tvPriceDish.text = resources.getString(R.string.price_style_usd, dish.price)
            tvDescription.text = dish.description

            ibFavorite.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    checkDishInterface.putOrDelFavorite(
                        isFavorite, mainVM, dish.id, dish.name ?: ""
                    )
                    cancel()
                }
            }

            ibToBag.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch{
                    checkDishInterface.putOrDelBasket(
                        isInBasket, mainVM,  dish.id, dish.name ?: ""
                    )
                    cancel()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            //Log.d(my_tag, "w: $width, h: $height")
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        picassoJob = null
        _binding = null
    }

}