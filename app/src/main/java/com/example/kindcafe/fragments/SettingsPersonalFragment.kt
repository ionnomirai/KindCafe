package com.example.kindcafe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.databinding.FragSettingsPersonalBinding
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.cancel
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
    private var zodiacSignCurrent: String? = null

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

        val zodiacSigns = resources.getStringArray(R.array.zodiac_signs)
        val spinnerListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(parent?.id == R.id.spZodiacSign){
                    zodiacSignCurrent = parent.getItemAtPosition(position).toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val arrayAdapter = ArrayAdapter(requireContext(),R.layout.spinner_item, zodiacSigns)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spZodiacSign.apply {
            adapter = arrayAdapter
            onItemSelectedListener = spinnerListener
            setSelection(0)
        }

        binding.tvSettingsPersonal.setOnClickListener {
            Toast.makeText(context, zodiacSignCurrent, Toast.LENGTH_SHORT).show()
        }



        Log.d(my_tag, "onViewCreated")
        binding.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                mainVM.personal.collect{userInfo ->

                    userInfo.name?.let {name ->
                        etSetName.setText(name)
                        headerSetting.tvUserName.text = name
                    }

                    userInfo.surname?.let {surname ->
                        etSetSurname.setText(surname)
                    }

                    userInfo.birthday?.let {birthday ->
                        etSetBirthday.setText(birthday)
                    }

                    userInfo.email?.let {email ->
                        tvSetEmail.text = email
                    }

                    userInfo.signZodiac?.let {zodiacName ->
                        Log.d(my_tag, zodiacName)
                        binding.spZodiacSign.setSelection(zodiacSigns.indexOf(zodiacName))
                    }
                }
            }

        }

    }

    override fun onPause() {
        super.onPause()
        mainVM.viewModelScope.launch {
            var name = binding.etSetName.text.toString()
            if(name.isEmpty() || name.isBlank()){
                name = mainVM.personal.value.name
            }
            val surname = binding.etSetSurname.text.toString()
            val birthday = binding.etSetBirthday.text.toString()

            val userInfoTemp = mainVM.personal.value.copy(
                name = name,
                surname = surname,
                birthday = birthday,
                signZodiac = zodiacSignCurrent
            )

            mainVM.setPersonalDataLocal(userInfoTemp)
            mainVM.setData(name) // set name (old live data)
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