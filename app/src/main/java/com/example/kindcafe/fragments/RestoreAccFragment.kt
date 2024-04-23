package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.databinding.FragLoginBinding
import com.example.kindcafe.databinding.FragRestoreAccountBinding
import com.example.kindcafe.firebase.AccountHelper
import com.example.kindcafe.firebase.firebaseInterfaces.DefinitionOfStatus
import com.example.kindcafe.utils.GeneralAccessTypes

class RestoreAccFragment : Fragment(){
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragRestoreAccountBinding? = null
    private val binding
        get() : FragRestoreAccountBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    private lateinit var accountHelper: AccountHelper

    private val MY_TAG = "RestoreAccFragmentTag"

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragRestoreAccountBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        mainActivity.supportActionBar?.title = ""
        mainActivity.accessUpperPart(GeneralAccessTypes.CLOSE)  // close upper part (tb title and icons)
        mainActivity.accessBottomPart(GeneralAccessTypes.CLOSE) // close bottom part

        accountHelper = AccountHelper(mainActivity, R.id.lDrawLayoutMain)

        val defStatus = object : DefinitionOfStatus {
            override fun onSuccess() {
                try {
                    val curFrag = parentFragmentManager.findFragmentById(R.id.fcv_main) as? RestoreAccFragment
                    if(curFrag != null){
                        Log.d(MY_TAG, "RestoreActt: $curFrag")
                       findNavController().popBackStack()
                    }
                } catch (e: Exception){
                    /* If user close screen earlier than it would auto*/
                    Log.d(MY_TAG, "RestoreActt exception: $e")
                }
            }
        }

        binding.apply {
            cvLoginGo.setOnClickListener {
                accountHelper.restoreAccount(
                    email = etRestoreLoginEmail.text.toString(),
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