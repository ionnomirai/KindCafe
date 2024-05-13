package com.example.kindcafe.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.kindcafe.data.Categories
import com.example.kindcafe.databinding.DialogSearchGeneralBinding
import com.example.kindcafe.utils.Locations


class DialogSearchGeneral(private val location: String) : DialogFragment() {
    private val my_tag = "DialogFragmentTag"

    private lateinit var binding: DialogSearchGeneralBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogSearchGeneralBinding.inflate(inflater, container, false)
        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitleDialog.text = when(location){
            Locations.FAVORITES.nameL -> Locations.FAVORITES.nameL
            Locations.BASKET.nameL -> Locations.BASKET.nameL
            Locations.SHOW_SPARKLING_DRINKS.nameL -> Locations.SHOW_SPARKLING_DRINKS.nameL
            Locations.SHOW_NON_SPARKLING_DRINKS.nameL -> Locations.SHOW_NON_SPARKLING_DRINKS.nameL
            Locations.SHOW_SWEETS.nameL -> Locations.SHOW_SWEETS.nameL
            Locations.SHOW_CAKES.nameL -> Locations.SHOW_CAKES.nameL
            else -> Locations.HOME.nameL
        }

        binding.apply{
            ibCloseDialog.setOnClickListener {
                this@DialogSearchGeneral.dismiss()
            }
            tvTitleDialog.setOnClickListener {
                DialogSearchGeneral(Locations.HOME.nameL).show(parentFragmentManager,null)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(my_tag, "dialog -- OnDestroy")
    }
}