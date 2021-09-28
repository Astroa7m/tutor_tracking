package com.example.tutortracking.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tutortracking.R
import com.example.tutortracking.databinding.ActivityMainBinding
import com.example.tutortracking.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var hasSessionStarted by Delegates.notNull<Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hasSessionStarted = true

        //configuring the appbar to hide the back button of some fragments
        val appBarConfig = AppBarConfiguration
            .Builder(
                R.id.profileFragment,
                R.id.studentsListFragment,
                R.id.loginFragment
            )
            .build()

        //setting the navigation with the actionbar
        setupActionBarWithNavController(findNavController(R.id.fragment_container), appBarConfig)

        //setting bottom navigation view to host the fragments which has the same id as the menu
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        // hiding the bottom nav controller in some fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.isVisible =
                !(destination.id == R.id.registerFragment || destination.id == R.id.loginFragment)
        }
    }
    //setting fragments label on appbar
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment_container)
        return navController.navigateUp()
    }

}