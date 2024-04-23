package com.example.kindcafe.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val nameData: MutableLiveData<String> = MutableLiveData()

    val numberOfAttemptsLive: MutableLiveData<Int> = MutableLiveData()

    var numberOfAttempts = 0
        private set

    fun setData(newName: String) {
        this.nameData.value = newName
    }

    fun setNumberOfAttempts(){
        numberOfAttemptsLive.value = numberOfAttempts
    }

    fun incAttempt() { numberOfAttempts++ }

    fun resetCounterAttempts(){ numberOfAttempts = 0}






}