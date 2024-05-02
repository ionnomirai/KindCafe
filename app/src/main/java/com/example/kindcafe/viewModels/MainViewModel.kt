package com.example.kindcafe.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kindcafe.data.Categories
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.KindCafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = KindCafeRepository.get()

    /*---------------------------------------------------------------------------------------------------*/
    /*------------------------------------------ Different funs -----------------------------------------*/

    val nameData: MutableLiveData<String> = MutableLiveData()

    val numberOfAttemptsLive: MutableLiveData<Int> = MutableLiveData()

    var numberOfAttempts = 0
        private set

    fun setData(newName: String) {
        this.nameData.value = newName
    }

    fun setNumberOfAttempts() {
        numberOfAttemptsLive.value = numberOfAttempts
    }

    fun incAttempt() {
        numberOfAttempts++
    }

    fun resetCounterAttempts() {
        numberOfAttempts = 0
    }

    /*---------------------------------------------------------------------------------------------------*/
    /*------------------------------------------ database data ------------------------------------------*/

    private var _sparklingDrinks: MutableStateFlow<List<Dish>> = MutableStateFlow(emptyList())
    val sparklingDrinks: StateFlow<List<Dish>>
        get() = _sparklingDrinks.asStateFlow()

    private var _cakes: MutableStateFlow<List<Dish>> = MutableStateFlow(emptyList())
    val cakes: StateFlow<List<Dish>>
        get() = _cakes.asStateFlow()

    suspend fun addDishLocal(dish: List<Dish>) {
        repository.insertDish(dish)
    }

    suspend fun getDishesByCategory(category: Categories) {
        when (category) {
            Categories.SparklingDrinks -> repository.getDishByCategory(category).collect {
                _sparklingDrinks.value = it
            }
            Categories.Cakes -> repository.getDishByCategory(category).collect{
                _cakes.value = it
            }
            Categories.Sweets -> ""
            Categories.NonSparklingDrinks -> ""
        }
    }
}