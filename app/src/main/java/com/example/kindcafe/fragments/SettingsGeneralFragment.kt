package com.example.kindcafe.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.databinding.FragSettingsGeneralBinding
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SettingsGeneralFragment: Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragSettingsGeneralBinding? = null
    private val binding
        get() : FragSettingsGeneralBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    /* Common viewModel between activity and this fragment */
    private val mainVM: MainViewModel by activityViewModels()
    private val dbManager = DbManager()

    private val my_tag = "SettingsGeneralFragmentTag"

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragSettingsGeneralBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainAct = activity as? MainActivity

        mainAct?.let {
            it.binding.tbMain.menu.findItem(R.id.itbSearch).isVisible = false
            it.binding.tvToolbarTitle.text = ""
            it.supportActionBar?.title = ""
        }

        Log.d(my_tag, "onViewCreated")
        binding.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                mainVM.personal.collect{userInfo ->

                    // this null check need, because when logout, it is trigered and pull NullPointerException
                    userInfo.name?.let {name->
                        headerSetting.tvUserName.text = name
                    }


                    userInfo.phoneNumber?.let {phone ->
                        etSetPhone.setText(phone)
                    }

                    userInfo.location?.let {location ->
                        etSetLocation.setText(location)
                    }
                }
            }

            binding.apply {
                bLogOut.setOnClickListener {
                    mainAct?.let{
                        it.logoutAction()
                        findNavController().popBackStack(R.id.homeFragment, false)
                    }
                }

                bExit.setOnClickListener {
                    findNavController().popBackStack(R.id.homeFragment, false)
                    mainAct?.finish()
                }

                bPersonalData.setOnClickListener {
                    findNavController().navigate(R.id.action_settingsGeneralFragment_to_settingsPersonalFragment)
                }
            }
        }


    }

    override fun onPause() {
        super.onPause()
        mainVM.viewModelScope.launch {
            val phone = binding.etSetPhone.text.toString()
            val location = binding.etSetLocation.text.toString()
            val userInfoTemp = mainVM.personal.value.copy(phoneNumber = phone, location = location)

            mainVM.setPersonalDataLocal(userInfoTemp)
            dbManager.setPrimaryData(KindCafeApplication.myAuth.currentUser, userInfoTemp)
            cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(my_tag, "onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}