package com.example.kindcafe.fragments

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.databinding.FragSettingsPersonalBinding
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.launch

class SettingsPersonalFragment : Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragSettingsPersonalBinding? = null
    private val binding
        get() : FragSettingsPersonalBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    /* Common viewModel between activity and this fragment */
    private val mainVM: MainViewModel by activityViewModels()
    private val dbManager = DbManager()

    private val my_tag = "SettingsPersonalFragmentTag"

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragSettingsPersonalBinding.inflate(layoutInflater, container, false)
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


                }
            }

        }

        //binding.etSetEmail.inputType = InputType.TYPE_NULL

/*        binding.tvSettingsGen.setOnClickListener {
            Log.d(my_tag, "birthday ${binding.etSetBirthday.text.toString()}")
            Log.d(my_tag, "zodiac ${binding.etSetZodiac.text.toString()}")
            Log.d(my_tag, "email ${binding.etSetEmail.text.toString()}")

        }*/


    }

    override fun onPause() {
        super.onPause()
/*        mainVM.viewModelScope.launch {
            val phone = binding.etSetPhone.text.toString()
            val location = binding.etSetLocation.text.toString()
            val userInfoTemp = mainVM.personal.value.copy(phoneNumber = phone, location = location)

            mainVM.setPersonalDataLocal(userInfoTemp)
            dbManager.setPrimaryData(KindCafeApplication.myAuth.currentUser, userInfoTemp)
            cancel()
        }*/
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