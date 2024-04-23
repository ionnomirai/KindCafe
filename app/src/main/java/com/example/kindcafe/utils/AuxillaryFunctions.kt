package com.example.kindcafe.utils

import android.app.Activity
import android.util.Log
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kindcafe.R
import com.example.kindcafe.firebase.firebaseInterfaces.DefinitionOfStatus
import com.google.android.material.snackbar.Snackbar

object AuxillaryFunctions {
private val MY_TAG = "AuxillaryFunctionsTAG"
    /* Show message on the screen */
    fun showSnackBar(
        @StringRes messageText: Int,
        activity: Activity,
        @IdRes currentView: Int = R.id.lDrawLayoutMain,
        @StringRes closeButton: Int = R.string.close
    ){
        val snackbar: Snackbar = Snackbar.make(
            activity.findViewById(currentView),
            activity.resources.getText(messageText),
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar
            .setAction(closeButton) {
                snackbar.dismiss()
            }
            .show()
    }

    fun defaultDefinitionOfStatusInterface(
        frag: Fragment,
        direction: SimplePopDirections,
        @IdRes fragContainer: Int = R.id.fcv_main): DefinitionOfStatus{
        return object : DefinitionOfStatus{
            override fun onSuccess() {
                try {
                    val curFrag = frag.parentFragmentManager.findFragmentById(fragContainer)
                    when(direction){
                        SimplePopDirections.TOP_DESTINATION -> curFrag?.findNavController()?.popBackStack(R.id.homeFragment, false)
                        SimplePopDirections.PREVIOUS_DESTINATION -> curFrag?.findNavController()?.popBackStack()
                    }
                } catch (e: Exception){
                    /* If user close screen earlier than it would auto*/
                    Log.d(MY_TAG, "LoginFrag exception: $e")
                }
            }
        }
    }

/*    val defStatus = object : DefinitionOfStatus {
        override fun onSuccess() {
            try {
                val curFrag = parentFragmentManager.findFragmentById(R.id.fcv_main) as? LoginFragment
                if(curFrag != null){
                    Log.d(MY_TAG, "LoginFrag: $curFrag")
                    findNavController().popBackStack()
                }
            } catch (e: Exception){
                *//* If user close screen earlier than it would auto*//*
                Log.d("MY_TAG", "LoginFrag exception: $e")
            }
        }
    }*/
}