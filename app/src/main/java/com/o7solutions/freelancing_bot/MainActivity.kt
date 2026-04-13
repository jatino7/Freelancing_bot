package com.o7solutions.freelancing_bot

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.o7solutions.freelancing_bot.databinding.ActivityMainBinding
import com.o7solutions.freelancing_bot.utils.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Use the toolbar from the binding
        setSupportActionBar(binding.toolbar)

        val headerView = binding.navDrawerView.getHeaderView(0)
        val tvName = headerView.findViewById<TextView>(R.id.tv_nav_name)
        val tvEmail = headerView.findViewById<TextView>(R.id.tv_nav_email)

// If you are using Firebase Auth
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        user?.let {
            tvEmail.text = it.email
            // If you saved the name in shared prefs or firebase, set it here:
            // tvName.text = "Welcome Back!"
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navDrawerView

        // Crucial: Use the FragmentContainerView/Fragment ID from your XML
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as androidx.navigation.fragment.NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.companyFragment,
                R.id.navigation_home, R.id.jobsFragment, R.id.navigation_dashboard,
                R.id.navigation_notifications, R.id.jobHistoryFragment, R.id.profileFragment
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val userType = getSharedPreferences(Constants.userKey, MODE_PRIVATE).getInt("userType", -1)

        // Use the actual menu instance from navView
        val menu = navView.menu
        if (userType == 1) {
            menu.findItem(R.id.navigation_dashboard)?.isVisible = false
            menu.findItem(R.id.jobsFragment)?.isVisible = false
            menu.findItem(R.id.companyFragment).isVisible = false

        } else {
            menu.findItem(R.id.jobHistoryFragment)?.isVisible = false
        }
    }

    // This allows the hamburger icon to open the drawer
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}