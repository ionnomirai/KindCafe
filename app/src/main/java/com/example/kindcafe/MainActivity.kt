package com.example.kindcafe

import android.os.Bundle
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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kindcafe.databinding.ActivityMainBinding
import com.example.kindcafe.firebase.AccountHelper
import com.example.kindcafe.fragments.HomeFragment
import com.example.kindcafe.fragments.RegistrationFragment
import com.example.kindcafe.utils.GeneralAccessTypes
import com.example.kindcafe.viewModels.MainViewModel


class MainActivity : AppCompatActivity()/*, NavigationView.OnNavigationItemSelectedListener*/{

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
    private lateinit var navController: NavController

    private val MY_TAG = "MainActivityTag"
    private val accountHelper = AccountHelper(this, R.id.lDrawLayoutMain)

    /* Common viewModel to get data to fragment (for example Home_fragment) */
    private val mainViewModel : MainViewModel by viewModels()

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

        binding.ibHome.setOnClickListener {
            Toast.makeText(this, "IB", Toast.LENGTH_SHORT).show()
        }

        everyOpenHomeSettings()

        movingLogicN2()

        updateMainUI()
    }

    /* Custom logic of moving between fragments */
    private  fun movingLogicN2(){

        binding.nvLeft.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.itemRegistrationFragment -> moveTo(R.id.action_homeFragment_to_registrationFragment)
                R.id.itemLogout -> {
                    if(accountHelper.signOut()) { // if we logout successfuly
                        binding.lDrawLayoutMain.closeDrawer(GravityCompat.START)
                        updateMainUI()
                    }
                }
                else -> Toast.makeText(this, "fraeg", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    /* Simple wrap for fragment moving. Needed to reduce code. */
    private fun moveTo(@IdRes idDest: Int, needCloseSideMenu: Boolean = true){
        navController.navigate(idDest)
        if(needCloseSideMenu){
            binding.lDrawLayoutMain.closeDrawer(GravityCompat.START)
        }
    }

    /* Update:
    * - toolbar header - change name;
    * - viewmodels.name -> give this name to fragment Home*/
    private fun updateMainUI(){
        val currentEmail = accountHelper.getUserEmail()
        binding.apply {
            if(currentEmail != null){
                nvLeft.getHeaderView(0).findViewById<TextView>(R.id.tvUserName).text = currentEmail
                mainViewModel.setData(currentEmail)
            } else {
                nvLeft.getHeaderView(0).findViewById<TextView>(R.id.tvUserName).text = resources.getString(R.string.default_username)
                mainViewModel.setData(resources.getString(R.string.default_username))
            }
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
    fun accessBottomPart(action: GeneralAccessTypes){
        binding.apply {
            when(action){
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

    fun accessUpperPart(action: GeneralAccessTypes){
        binding.apply {
            when(action){
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

}