package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.databinding.FragHomeBinding
import com.example.kindcafe.databinding.FragLoginBinding
import com.example.kindcafe.firebase.AccountHelper
import com.example.kindcafe.firebase.firebaseInterfaces.DefinitionOfStatus
import com.example.kindcafe.utils.GeneralAccessTypes
import com.example.kindcafe.viewModels.MainViewModel

class LoginFragment : Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragLoginBinding? = null
    private val binding
        get() : FragLoginBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    private lateinit var accountHelper: AccountHelper

    private val MY_TAG = "LoginFragmentTag"

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragLoginBinding.inflate(layoutInflater, container, false)
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
                    val curFrag = parentFragmentManager.findFragmentById(R.id.fcv_main) as? LoginFragment
                    if(curFrag != null){
                        Log.d(MY_TAG, "LoginFrag: $curFrag")
                        findNavController().popBackStack()
                    }
                } catch (e: Exception){
                    /* If user close screen earlier than it would auto*/
                    Log.d("MY_TAG", "LoginFrag exception: $e")
                }
            }
        }

        binding.apply {

            /* The user enters his email and password and logs in to his account */
            cvLoginGo.setOnClickListener {
                accountHelper.signInWithEmail(
                    email = etLoginEmail.text.toString(),
                    password = etLoginPassword.text.toString(),
                    status = defStatus
                )
            }

            /* Move to RestoreAccFragment If user forgot password */
            tvLoginForgot.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_restoreAccFragment)
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
