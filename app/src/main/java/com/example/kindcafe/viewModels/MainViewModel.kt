package com.example.kindcafe.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kindcafe.data.Categories
import com.example.kindcafe.database.Dish
import com.example.kindcafe.database.Favorites
import com.example.kindcafe.database.KindCafeRepository
import com.example.kindcafe.database.UserPersonal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect


class MainViewModel : ViewModel() {
    private val repository = KindCafeRepository.get()
    private val my_tag = "MainViewModel"

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


    /*----------------------------------dishes-----------------------------------------------*/
    private var _allDishes: MutableStateFlow<List<Dish>> = MutableStateFlow(emptyList())
    val allDishes: StateFlow<List<Dish>>
        get() = _allDishes.asStateFlow()

    private var _sparklingDrinks: MutableStateFlow<List<Dish>> = MutableStateFlow(emptyList())
    val sparklingDrinks: StateFlow<List<Dish>>
        get() = _sparklingDrinks.asStateFlow()

    private var _cakes: MutableStateFlow<List<Dish>> = MutableStateFlow(emptyList())
    val cakes: StateFlow<List<Dish>>
        get() = _cakes.asStateFlow()

    private var _currentDish: MutableStateFlow<Dish> = MutableStateFlow(Dish())
    val currentDish: StateFlow<Dish>
        get() = _currentDish

    /*----------------------------------favorites-----------------------------------------------*/

    private var _favorites: MutableStateFlow<List<Favorites>> = MutableStateFlow(emptyList())
    val favorites: StateFlow<List<Favorites>>
        get() = _favorites

    private var _favoritesLikeDish: MutableStateFlow<List<Dish>> = MutableStateFlow(emptyList())
    val favoritesLikeDish: StateFlow<List<Dish>>
        get() = _favoritesLikeDish

    /*----------------------------------personal-----------------------------------------------*/
    private var _personal: MutableStateFlow<UserPersonal> = MutableStateFlow(UserPersonal())
    val personal: StateFlow<UserPersonal>
        get() = _personal



    /*----------------------------------FUN dishes-----------------------------------------------*/
    suspend fun addDishLocal(dish: List<Dish>) {
        repository.insertDish(dish)
    }

    suspend fun getAllDishes(){
        repository.getAllDishes().collect{
            _allDishes.value = it
        }
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

    suspend fun getDish(id: String, name: String){
         repository.getDish(id, name).collect{
             _currentDish.value = it
         }
    }

    /*----------------------------------FUN favorites-----------------------------------------------*/

    suspend fun getLikeDish(id: String, name: String): Dish{
        return repository.getDish1(id, name)
    }

    suspend fun addFavoritesLocal(favoriteDish: Favorites) {
        repository.insertFavorite(favoriteDish)
    }

    suspend fun getAllFavorites(){
        repository.getFavorites().collect{
            _favorites.value = it
        }
    }

    fun addToFavLikeDish(data: List<Dish>){
        _favoritesLikeDish.value = data
    }

    suspend fun deleteFavDish(fav: Favorites){
        repository.deleteFavDish(fav)
    }

    suspend fun deleteAllFavorites(){
        repository.deleteAllFav()
    }

    /*----------------------------------FUN personal-----------------------------------------------*/

    suspend fun getPersonalDataLocal(){
        repository.getPersonalDataLocal().collect{
            _personal.value = it
        }
    }

    suspend fun setPersonalDataLocal(personal: UserPersonal){
        repository.setPersonalDataLocal(personal)
    }

    suspend fun deletePersonalDataLocal(personal: UserPersonal){
        repository.deletePersonalDataLocal(personal)
    }

    suspend fun deleteAllPersonal(){
        repository.deleteAllPersonal()
    }

    /*----------------------------------FUN common-----------------------------------------------*/

    suspend fun deleteAllLogout(){
        deleteAllPersonal()
        deleteAllFavorites()
    }


}