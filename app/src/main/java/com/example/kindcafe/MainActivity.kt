package com.example.kindcafe

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kindcafe.data.AllUserData
import com.example.kindcafe.database.Dish
import com.example.kindcafe.databinding.ActivityMainBinding
import com.example.kindcafe.dialogs.DialogSearchGeneral
import com.example.kindcafe.dialogs.ProgressUpdateMain
import com.example.kindcafe.firebase.AccountHelper
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.firebase.StorageManager
import com.example.kindcafe.firebase.firebaseEnums.UriSize
import com.example.kindcafe.firebase.firebaseInterfaces.GetUrisCallback
import com.example.kindcafe.firebase.firebaseInterfaces.ReadAllData
import com.example.kindcafe.firebase.firebaseInterfaces.ReadUsersData
import com.example.kindcafe.utils.GeneralAccessTypes
import com.example.kindcafe.viewModels.MainViewModel
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    /*---------------------------------------- Properties ----------------------------------------*/

    lateinit var binding: ActivityMainBinding

    /*  AppBarConfiguration:
    * - determining which drawerLayout to work with;
    * - definition of the "Burger" button;
    * - definition of top level destination;
    * - control of the back button for further transitions. */
    private lateinit var appBarConfiguration: AppBarConfiguration

    /* NavController - This will be the object that keeps track of the current navigation position
    * among the navGraph. It also changes fragments in NavHostFrament*/
    lateinit var navController: NavController

    private val my_tag = "MainActivityTag"
    private val cacheSize: Long = 2048 * 2048 * 50 //+-209 MB
    private lateinit var picasso: Picasso

    private val accountHelper = AccountHelper(this, R.id.lDrawLayoutMain)

    /* Common viewModel to get data to fragment (for example Home_fragment) */
    val mainVM: MainViewModel by viewModels()

    private val dbManager = DbManager()
    private val storageManager = StorageManager()

    private val listAllDishes = mutableListOf<Dish>()
    private val listSmallUris = mutableListOf<Dish>()
    private val listBigUris = mutableListOf<Dish>()
    private var listUserInfo = AllUserData()

    private val isDbServerDLDone = MutableStateFlow(false)
    private val isSmallUrisDone = MutableStateFlow(false)
    private val isBigUrisDone = MutableStateFlow(false)
    private val isUsersInfoDone = MutableStateFlow(false)

    private lateinit var pbUpdate: AlertDialog

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Connecting my custom Action Bar */
        setSupportActionBar(binding.tbMain)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment

        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            navGraph = navController.graph,
            drawerLayout = binding.lDrawLayoutMain
        )

        //setupNavigationMenu(navController)
        setupActionBar(navController, appBarConfiguration)

        /* When user name changed -- call this */
        mainVM.nameData.observe(this) {
            updateMainUI()
        }

        binding.apply {
            ibHome.setOnClickListener {
                navController.popBackStack(R.id.homeFragment, false)
            }

            ibBag.setOnClickListener {
                if (KindCafeApplication.myAuth.currentUser != null) {
                    Log.d(my_tag, "orderplaced: ${mainVM.orderPlaced.value.toString()}")
                    if(mainVM.orderPlaced.value.isEmpty()){
                        navController.popBackStack(R.id.homeFragment, false)
                        navController.navigate(R.id.action_homeFragment_to_basketFrag)
                    } else {
                        navController.popBackStack(R.id.homeFragment, false)
                        navController.navigate(R.id.action_homeFragment_to_orderSummaryFragment)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Please login", Toast.LENGTH_SHORT).show()
                }
            }

            ibHeart.setOnClickListener {
                if (KindCafeApplication.myAuth.currentUser != null) {
                    navController.popBackStack(R.id.homeFragment, false)
                    navController.navigate(R.id.action_homeFragment_to_favoriteFragment)
                } else {
                    Toast.makeText(this@MainActivity, "Please login", Toast.LENGTH_SHORT).show()
                }

            }

            ibSettings.setOnClickListener {
                if (KindCafeApplication.myAuth.currentUser != null){
                    navController.popBackStack(R.id.homeFragment, false)
                    navController.navigate(R.id.action_homeFragment_to_settingsGeneralFragment)
                }
            }

            ibProfile.setOnClickListener {
                if (KindCafeApplication.myAuth.currentUser != null){
                    navController.popBackStack(R.id.homeFragment, false)
                    navController.navigate(R.id.action_homeFragment_to_settingsPersonalFragment)
                }
            }
        }

        everyOpenHomeSettings()

        movingLogicN2()

        /* Set new cache size for Picasso */
        picasso = Picasso
            .Builder(this)
            .downloader(OkHttp3Downloader(this, cacheSize))
            .build()

        downloadDbWhenStart()

        doWhenStartOrLogin()

        downloadLocalDb()
    }

    /* Custom logic of moving between fragments */
    private fun movingLogicN2() {

        binding.nvLeft.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.itemLogin -> {
                    if (!accountHelper.isUserLogin()) {
                        moveTo(R.id.action_homeFragment_to_loginFragment)
                    }
                }

                R.id.itemRegistrationFragment -> {
                    if (!accountHelper.isUserLogin()) {
                        moveTo(R.id.action_homeFragment_to_registrationFragment)
                    }
                }

                R.id.itemFavorites ->{
                    if (KindCafeApplication.myAuth.currentUser != null){
                        moveTo(R.id.action_homeFragment_to_favoriteFragment)
                    }
                }

                R.id.itemBasket ->{
                    if (KindCafeApplication.myAuth.currentUser != null){
                        moveTo(R.id.action_homeFragment_to_basketFrag)
                    }
                }

                R.id.itemSettings ->{
                    if (KindCafeApplication.myAuth.currentUser != null){
                        moveTo(R.id.action_homeFragment_to_settingsGeneralFragment)
                    }
                }

                R.id.itemLogout -> { logoutAction() }
            }
            true
        }
    }

    /* Simple wrap for fragment moving. Needed to reduce code. */
    private fun moveTo(@IdRes idDest: Int, needCloseSideMenu: Boolean = true) {
        navController.navigate(idDest)
        if (needCloseSideMenu) {
            binding.lDrawLayoutMain.closeDrawer(GravityCompat.START)
        }
    }

    /* Update:
    * - toolbar header - change name;
    * - viewmodels.name -> give this name to fragment Home*/
    fun updateMainUI() {
        binding.apply {
            nvLeft
                .getHeaderView(0)
                .findViewById<TextView>(R.id.tvUserName).text = mainVM.nameData.value
        }
    }

    /* Perform these settings every time the screen starts up */
    fun everyOpenHomeSettings() {
        binding.tbMain.title = ""
        binding.tvToolbarTitle.text = resources.getString(R.string.home_name)

        accessBottomPart(GeneralAccessTypes.OPEN)
        accessUpperPart(GeneralAccessTypes.OPEN)
    }

    /* Hide or show bottom menu */
    fun accessBottomPart(action: GeneralAccessTypes) {
        binding.apply {
            when (action) {
                GeneralAccessTypes.OPEN -> {
                    clMainBottomMenu.visibility = View.VISIBLE
                    ibHome.visibility = View.VISIBLE
                }

                GeneralAccessTypes.CLOSE -> {
                    clMainBottomMenu.visibility = View.GONE
                    ibHome.visibility = View.GONE
                }
            }
        }
    }

    fun accessUpperPart(action: GeneralAccessTypes) {
        binding.apply {
            when (action) {
                GeneralAccessTypes.OPEN -> {
                    tvToolbarTitle.isVisible = true
                    tbMain.menu.findItem(R.id.itbSearch).isVisible = true
                }

                GeneralAccessTypes.CLOSE -> {
                    tvToolbarTitle.isVisible = false
                    tbMain.menu.findItem(R.id.itbSearch).isVisible = false
                }
            }
        }
    }

    /* Connecting NavigationView (sliding panel) to toolbar (accessed via appBarConfiguration). This does the following:
    *  - shows the current location in place title (toolbar);
    *  - shows the back button when we are not in the top destination;
    *  - shows the "burger" button when we are in the top destination. */
    private fun setupActionBar(
        navController: NavController,
        appBarConfiguration: AppBarConfiguration
    ) {
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /* Handling back button click */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu_general, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itbSearch) {
            DialogSearchGeneral(mainVM.currentLocation).show(supportFragmentManager, null)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun downloadDbWhenStart() {
        /* Get data from RealtimeDB*/
        dbManager.readAllDishDataFromDb(getCallbackReadAllData())

        /* Get Data about small uri */
        mainVM.viewModelScope.launch {
            isDbServerDLDone.collect {
                if (it) {
                    Log.d(my_tag, "downloadDbWhenStart: downloadDbWhenStart")
                    storageManager.readUri(
                        listAllDishes,
                        getUrisBySize(UriSize.Small, this),
                        UriSize.Small
                    )
                }
            }
        }

        /* Get Data about big uri */
        mainVM.viewModelScope.launch {
            isSmallUrisDone.collect {
                if (it) {
                    storageManager.readUri(
                        listSmallUris,
                        getUrisBySize(UriSize.Big, this),
                        UriSize.Big
                    )
                    Log.d(my_tag, "isSmallUrisDone")
                }
            }
        }

        /* Get Data about big uri */
        mainVM.viewModelScope.launch {
            isBigUrisDone.collect {
                if (it) {
                    mainVM.addDishLocal(listBigUris)
                    Log.d(my_tag, "Home-ViewModel added to local DB added")
                    pbUpdate.dismiss()
                    isDbServerDLDone.value = false
                    isSmallUrisDone.value = false
                    isBigUrisDone.value = false
                    Log.d(my_tag, "Home-ViewModel get to local DB added")
                    cancel()
                }
            }
        }
    }

    private fun getCallbackReadAllData(): ReadAllData {
        return object : ReadAllData {
            override fun readAll(data: List<Dish>) {
                listAllDishes.clear()
                listAllDishes.addAll(data)
                Log.d(my_tag, "Main-ViewModel data added")
                isDbServerDLDone.value = true
                pbUpdate = ProgressUpdateMain.createProgressDialog(this@MainActivity)
            }
        }
    }

    private fun getUrisBySize(uriSize: UriSize, job: CoroutineScope): GetUrisCallback {
        return object : GetUrisCallback {
            override fun getUris(newData: List<Dish>) {
                if (uriSize == UriSize.Small) {
                    listSmallUris.clear()
                    listSmallUris += newData
                    isSmallUrisDone.value = true
                    Log.d(my_tag, "Main-ViewModel Uri small added")
                } else {
                    listBigUris.clear()
                    listBigUris += newData
                    isBigUrisDone.value = true
                    Log.d(my_tag, "Main-ViewModel Uri big added")
                }
                job.cancel()
            }
        }
    }

    fun doWhenStartOrLogin() {

        val user = KindCafeApplication.myAuth.currentUser
        if (user != null) {
            // read data about personal
            dbManager.readUsersData(user, object : ReadUsersData {
                override fun readAllUserData(data: AllUserData) {
                    Log.d(my_tag, "Start or Login: in callback")
                    listUserInfo = data
                    isUsersInfoDone.value = true
                }
            })

            // write into local db
            lifecycleScope.launch {
                isUsersInfoDone.collect{
                    if(it){
                        Log.d(my_tag, "doWhenStartOrLogin: doWhenStartOrLogin")
                        mainVM.deleteAllFavorites()
                        mainVM.deleteAllPersonal()

                        mainVM.deleteAllOrderItemsLocal()
                        mainVM.deleteAllOrderPlacedLocal()

                        listUserInfo.orderBasket?.let {orderBList ->
                            // if we downloaded from server, then delete local (we will write further)
                            //mainVM.deleteAllOrderItemsLocal()
                            orderBList.forEach {
                                mainVM.addOrderItemsLocal(it)
                            }
                            Log.d(my_tag, "Start or Login: update basket from server")
                        }

                        listUserInfo.orderPlaced?.let {orderPList ->
                            // if we downloaded from server, then delete local (we will write further)
                            //mainVM.deleteAllOrderItemsLocal()
                            Log.d(my_tag, "Start or Login-- orderPList: ${orderPList}")
                            orderPList.forEach {
                                mainVM.addOrderPlacedLocal(it)
                            }
                            Log.d(my_tag, "Start or Login: update placed from server")
                        }

                        listUserInfo.personal?.let {
                            mainVM.setPersonalDataLocal(it)
                            mainVM.setData(it.name)
                            Log.d(my_tag, "Start or Login: update personal")
                        }
                        listUserInfo.favorites?.let {
                            for (i in it){
                                mainVM.addFavoritesLocal(i)
                                Log.d(my_tag, "Start or Login: in favorite")
                            }
                        }
                        isUsersInfoDone.value = false
                        Log.d(my_tag, "Start or Login: person and fav done")
                    }
                }
            }



            // read data about order
            // write into local db
        } else {
            mainVM.setData(resources.getString(R.string.default_username))
        }
    }

    fun logoutAction(){
        if (accountHelper.signOut()) { // if we logout successfuly
            binding.lDrawLayoutMain.closeDrawer(GravityCompat.START)
            mainVM.setData(resources.getString(R.string.default_username))
            doWhenLogout()
        }
    }
    private fun doWhenLogout() {
        lifecycleScope.launch {
            mainVM.deleteAllLogout()
            cancel()
        }
    }

    private fun downloadLocalDb(){
        lifecycleScope.launch {
            Log.d(my_tag, "Local: getAllFavorites()")
            mainVM.getAllFavorites()
        }

        lifecycleScope.launch {
            Log.d(my_tag, "Local: getBasketLocal()")
            mainVM.getOrderItemsLocal()
        }

        lifecycleScope.launch {
            Log.d(my_tag, "Local: getOrderPlacedLocal()")
            mainVM.getOrderPlacedLocal()
        }

        lifecycleScope.launch {
            Log.d(my_tag, "Local: getAllDishes()")
            mainVM.getAllDishes()
        }

        lifecycleScope.launch {
            Log.d(my_tag, "Local: getOrderPlacedLocal()")
            mainVM.getOrderPlacedLocal()
        }

        lifecycleScope.launch {
            Log.d(my_tag, "Local: getPersonal()")
            mainVM.getPersonalDataLocal()
        }
    }
}