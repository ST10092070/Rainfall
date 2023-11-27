package com.corne.rainfall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.corne.rainfall.data.preference.IRainfallPreference
import com.corne.rainfall.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A class that represents the main activity of the Rainfall app.
 *
 * This activity serves as the entry point of the application's user interface. It hosts
 * the navigation graph and sets up the navigation controller for the app.
 *
 * The activity is annotated with [AndroidEntryPoint], enabling Hilt for dependency injection.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    @Inject
    lateinit var rainfallPreferenceManager: IRainfallPreference

    /**
     * Initializes the activity and sets up the user interface and navigation components.
     *
     * @param savedInstanceState The saved instance state passed in.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the navigation controller.
        val navHostFragment = binding.navHostFragmentActivityMain.getFragment<NavHostFragment>()
        val navController = navHostFragment.navController

        // Configure the app bar behavior.
        with(navController) {
            appBarConfiguration = AppBarConfiguration(graph)
            binding.bottomNavigationView.setupWithNavController(this)
//            val badge = binding.bottomNavigationView.getOrCreateBadge(R.id.navigation_maps)
//            badge.isVisible = true
//            badge.number = 99
        }

        // Set up a click listener for the FAB.
        binding.fab.setOnClickListener {
            navController.navigate(R.id.action_global_navigation_add)
        }

        // Set up a listener for destination changes. We might want to hide the nav bar when on certain page.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            println(destination.id)
        }

        // Observe and handle dark mode preference changes.
        observeDarkModePreference()

        // Observe offline mode preference changes.
        observeOfflineModePreference()

        //Remove the background of the bottom navigation view.
        binding.bottomNavigationView.background = null
    }

    /**
     * Observes and handles changes in the dark mode preference.
     */
    private fun observeDarkModePreference() {
        rainfallPreferenceManager.uiModeFlow.asLiveData().observe(this) {
            val mode = when (it) {
                true -> AppCompatDelegate.MODE_NIGHT_YES
                false -> AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    /**
     * Observes and handles changes in the offline mode preference.
     */
    public fun observeOfflineModePreference() {
//        AppCompatDelegate.setApplicationLocales()
    }


}