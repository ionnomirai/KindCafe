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
import com.example.kindcafe.utils.AuxillaryFunctions
import com.example.kindcafe.utils.GeneralAccessTypes
import com.example.kindcafe.utils.SimplePopDirections
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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

    private val mainVM : MainViewModel by activityViewModels()

    private var job : Job? = null

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

        /* interface */
        val defStatus = AuxillaryFunctions.defaultDefinitionOfStatusInterface(this, SimplePopDirections.PREVIOUS_DESTINATION)

        binding.apply {

            /* The user enters his email and password and logs in to his account */
            cvLoginGo.setOnClickListener {
                job = CoroutineScope(Dispatchers.Main).launch {
                    accountHelper.signInWithEmail(
                        email = etLoginEmail.text.toString(),
                        password = etLoginPassword.text.toString(),
                        status = defStatus
                    )
                }
            }

            cvTryAgain.setOnClickListener {
                clLoginOk.visibility = View.VISIBLE
                clTooManyAttempt.visibility = View.GONE
            }

            /* Move to RestoreAccFragment If user forgot password */
            tvRegNow.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_restoreAccFragment)
            }

            /* When user do 5 wrong attempt, we offer him to restore account*/
            mainVM.numberOfAttemptsLive.observe(viewLifecycleOwner){
                Log.d(MY_TAG, "Attempts: ${mainVM.numberOfAttempts}")
                if (mainVM.numberOfAttempts == 5) {
                    clLoginOk.visibility = View.GONE
                    clTooManyAttempt.visibility = View.VISIBLE
                    mainVM.resetCounterAttempts()
                }
            }
        }


    }

    override fun onResume() {
        super.onResume()
        Log.d(MY_TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(MY_TAG, "onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(MY_TAG, "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(MY_TAG, "onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(MY_TAG, "onDetach")
    }
}
