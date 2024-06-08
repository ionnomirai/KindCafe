package com.example.kindcafe.interfaces

import android.content.res.ColorStateList
import android.widget.ImageButton
import com.example.kindcafe.viewModels.MainViewModel

/* Interface designed for use in fragments with additional information about dishes. In this case,
it allows you to avoid duplicating code for the dialog and fragment.*/
interface DetailedCheckFavoritesBasket {
    fun checkAndFillFavorites (isFavorite: Boolean, ib: ImageButton, colorActive: ColorStateList, colorNonActive: ColorStateList)
    fun checkAndFillBasket (isInBasket: Boolean, ib: ImageButton, colorActive: ColorStateList, colorNonActive: ColorStateList)

    suspend fun putOrDelFavorite(isFavorite: Boolean, mainVm: MainViewModel, idDish: String, nameDish: String)
    suspend fun putOrDelBasket(isInBasket: Boolean, mainVm: MainViewModel, idDish: String, nameDish: String)
}