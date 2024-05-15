package com.example.kindcafe.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.kindcafe.R
import com.example.kindcafe.database.Dish
import com.example.kindcafe.databinding.FragDishDetailBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DialogDetailedDish(private val dish: Dish): DialogFragment() {
    private val my_tag = "DialogFragmentTag"

    private var _binding: FragDishDetailBinding? = null
    private val binding
        get(): FragDishDetailBinding {
            return checkNotNull(_binding){
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    private var picassoJob: Job? = null

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

        binding.apply {
            ibCloseDialogD.visibility = View.VISIBLE
            ibCloseDialogD.setOnClickListener { this@DialogDetailedDish.dismiss() }

            picassoJob = this@DialogDetailedDish.lifecycleScope.launch {
                Picasso.get().load(dish.uriBig).into(ivPicBig)
            }

            tvNameDish.text = dish.name
            tvPriceDish.text = resources.getString(R.string.price_style_usd, dish.price)
            tvDescription.text = dish.description
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