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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kindcafe.database.Dish
import com.example.kindcafe.databinding.ActivityMainBinding
import com.example.kindcafe.firebase.AccountHelper
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.firebase.StorageManager
import com.example.kindcafe.firebase.firebaseEnums.UriSize
import com.example.kindcafe.firebase.firebaseInterfaces.GetUrisCallback
import com.example.kindcafe.firebase.firebaseInterfaces.ReadAllData
import com.example.kindcafe.utils.GeneralAccessTypes
import com.example.kindcafe.viewModels.MainViewModel
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity()/*, NavigationView.OnNavigationItemSelectedListener*/ {

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
    private lateinit var picasso : Picasso

    private val accountHelper = AccountHelper(this, R.id.lDrawLayoutMain)

    /* Common viewModel to get data to fragment (for example Home_fragment) */
    val mainVM: MainViewModel by viewModels()

    private val dbManager = DbManager()
    private val storageManager = StorageManager()

    private val listAllDishes = mutableListOf<Dish>()
    private val listSmallUris = mutableListOf<Dish>()
    private val listBigUris = mutableListOf<Dish>()

    private val isDbServerDLDone = MutableStateFlow(false)
    private val isSmallUrisDone = MutableStateFlow(false)
    private val isBigUrisDone = MutableStateFlow(false)

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

        initialUISettingMain()

        /* When user name changed -- call this */
        mainVM.nameData.observe(this) {
            updateMainUI()
        }

        binding.ibHome.setOnClickListener {
            val verif = accountHelper.myAuth.currentUser?.isEmailVerified
            Toast.makeText(this, "$verif", Toast.LENGTH_SHORT).show()
        }

        everyOpenHomeSettings()

        movingLogicN2()

        /* Set new cache size for Picasso */
        picasso = Picasso
            .Builder(this)
            .downloader(OkHttp3Downloader(this, cacheSize))
            .build()

        downloadDbWhenStart()
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
                R.id.itemLogout -> {
                    if (accountHelper.signOut()) { // if we logout successfuly
                        binding.lDrawLayoutMain.closeDrawer(GravityCompat.START)
                        mainVM.setData(resources.getString(R.string.default_username))
                    }
                }

                else -> Toast.makeText(this, "fraeg", Toast.LENGTH_SHORT).show()
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

    /* Initinal UI main setting */
    private fun initialUISettingMain(){
        val currentEmail = accountHelper.getUserEmail()
        if (currentEmail != null) {
            mainVM.setData(currentEmail)
        } else {
            mainVM.setData(resources.getString(R.string.default_username))
        }
    }

    /* Update:
    * - toolbar header - change name;
    * - viewmodels.name -> give this name to fragment Home*/
    fun updateMainUI() {
        Log.d(my_tag, "updateMainUI ")
        //val currentEmail = accountHelper.getUserEmail()
        binding.apply {
            nvLeft
                .getHeaderView(0)
                .findViewById<TextView>(R.id.tvUserName).text = mainVM.nameData.value
        }
    }

    /* Perform these settings every time the screen starts up */
    fun everyOpenHomeSettings() {
        //binding.tbMain.title = "Kind Cafe"
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

    /* Connecting NavigationView (sliding panel) to navController so that you can navigate. */
    private fun setupNavigationMenu(navController: NavController) {
        binding.nvLeft.setupWithNavController(navController)
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
            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun downloadDbWhenStart(){
        /* Get data from RealtimeDB*/
        dbManager.readAllDishDataFromDb(getCallbackReadAllData())

        /* Get Data about small uri */
        mainVM.viewModelScope.launch {
            isDbServerDLDone.collect{
                if (it){
                    storageManager.readUri(listAllDishes, getUrisBySize(UriSize.Small, this), UriSize.Small)
                }
            }
        }

        /* Get Data about big uri */
        mainVM.viewModelScope.launch{
            isSmallUrisDone.collect{
                if (it){
                    storageManager.readUri(listSmallUris, getUrisBySize(UriSize.Big, this), UriSize.Big)
                }
            }
        }

        /* Get Data about big uri */
        mainVM.viewModelScope.launch {
            isBigUrisDone.collect{
                if(it){
                    mainVM.addDishLocal(listBigUris)
                    Log.d(my_tag, "Home-ViewModel added to local DB added")
                    mainVM.getAllDishes()
                    Log.d(my_tag, "Home-ViewModel get to local DB added")
                    isDbServerDLDone.value = false
                    isSmallUrisDone.value = false
                    isBigUrisDone.value = false
                    cancel()
                }
            }
        }

//        this only to me (display result) -- delete after all
/*        mainVM.viewModelScope.launch{
            mainVM.allDishes.collect{
                Log.d(my_tag, "All dishes: $it")
            }
        }*/
    }

    private fun getCallbackReadAllData(): ReadAllData {
        return object : ReadAllData {
            override fun readAll(data: List<Dish>) {
                listAllDishes.clear()
                listAllDishes.addAll(data)
                Log.d(my_tag, "Main-ViewModel data added")
                isDbServerDLDone.value = true
            }
        }
    }

    private fun getUrisBySize(uriSize: UriSize, job: CoroutineScope): GetUrisCallback {
        return object : GetUrisCallback {
            override fun getUris(newData: List<Dish>) {
                if(uriSize == UriSize.Small){
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
}