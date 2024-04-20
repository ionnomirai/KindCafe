package com.example.kindcafe.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val nameData: MutableLiveData<String> = MutableLiveData()

    fun setData(newName: String) {
        this.nameData.value = newName
    }
}