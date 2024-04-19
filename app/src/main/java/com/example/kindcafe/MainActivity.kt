package com.example.kindcafe

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kindcafe.databinding.ActivityMainBinding
import com.example.kindcafe.utils.GeneralAccessTypes


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
    private lateinit var navController: NavController

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Connecting my custom Action Bar */
        setSupportActionBar(binding.tbMain)

        /* set default active item bottom navigation on empty button */
        //binding.bnvMain.selectedItemId = R.id.bnvItemEmpty

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment

        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            navGraph = navController.graph,
            drawerLayout = binding.lDrawLayoutMain
        )

        binding.ibHome.setOnClickListener {
            Toast.makeText(this, "IB", Toast.LENGTH_SHORT).show()
        }


        setupNavigationMenu(navController)
        setupActionBar(navController, appBarConfiguration)
        //setupBottomNavMenu(navController)

        /* Full Screen */
        //setFullScreen()

        everyOpenHomeSettings()
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

/*
    private fun setupBottomNavMenu(navController: NavController) {
        binding.bnvMain.setupWithNavController(navController)
    }
*/

    /*    private fun setFullScreen(){
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, binding.root).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }*/

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