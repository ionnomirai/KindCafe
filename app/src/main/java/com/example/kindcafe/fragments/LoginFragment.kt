package com.example.kindcafe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.databinding.FragHomeBinding
import com.example.kindcafe.databinding.FragLoginBinding
import com.example.kindcafe.firebase.AccountHelper
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

        accountHelper = AccountHelper(mainActivity, R.id.constrLayoutLogin)

        binding.apply {

            cvLoginGo.setOnClickListener {
                if (accountHelper.signInWithEmail(
                    email = etLoginEmail.text.toString(),
                    password = etLoginPassword.text.toString()
                )){
                    mainActivity.navController.popBackStack()
                } else {
                    Toast.makeText(this@LoginFragment.context, "else", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
