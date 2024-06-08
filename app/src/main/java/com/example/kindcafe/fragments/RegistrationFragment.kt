package com.example.kindcafe.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.TEXT_ALIGNMENT_TEXT_START
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.databinding.FragRegistrationBinding
import com.example.kindcafe.firebase.AccountHelper
import com.example.kindcafe.firebase.firebaseInterfaces.DefinitionOfStatus
import com.example.kindcafe.utils.AuxillaryFunctions
import com.example.kindcafe.utils.GeneralAccessTypes
import com.example.kindcafe.utils.SimplePopDirections
import com.google.android.material.snackbar.Snackbar

class RegistrationFragment : Fragment() {
    private var _binding: FragRegistrationBinding? = null
    private val binding
        get() : FragRegistrationBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    private lateinit var accountHelper: AccountHelper

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragRegistrationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        mainActivity.supportActionBar?.title = ""
        mainActivity.accessUpperPart(GeneralAccessTypes.CLOSE)  // close upper part (tb title and icons)
        mainActivity.accessBottomPart(GeneralAccessTypes.CLOSE) // close bottom part

        /* initialise account helper for perform registrations */
        accountHelper = AccountHelper(mainActivity, R.id.lDrawLayoutMain)

        /* interface */
        val defStatus = AuxillaryFunctions.defaultDefinitionOfStatusInterface(this, SimplePopDirections.TOP_DESTINATION)

        binding.apply {


            /* Action when user press button "GO" */
            cvRegGo.setOnClickListener {
                accountHelper.signUpWithEmail(
                    name = etRegName.text.toString(),
                    email = etRegEmail.text.toString(),
                    password = etRegPassword.text.toString(),
                    status = defStatus
                )
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}