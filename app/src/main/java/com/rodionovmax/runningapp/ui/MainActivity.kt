package com.rodionovmax.runningapp.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rodionovmax.runningapp.R
import com.rodionovmax.runningapp.databinding.ActivityMainBinding
import com.rodionovmax.runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.rodionovmax.runningapp.other.Constants.KEY_FIREBASE_EMAIL
import com.rodionovmax.runningapp.other.Constants.KEY_FIREBASE_UID
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }
    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Timber.d("User is authenticated")
            sharedPref.edit()
                .putString(KEY_FIREBASE_EMAIL, currentUser.email.toString())
                .apply()
        } else {
            Timber.d("User is not authenticated")
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToTrackingFragmentIfNeeded(intent)

        setSupportActionBar(binding.toolbar)

        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        // to fix the bug that fragment reloads when clicking runs nav button
        binding.bottomNavigationView.setOnNavigationItemReselectedListener { /* NO-OP */ }

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                        binding.bottomNavigationView.visibility = View.VISIBLE
                    else -> binding.bottomNavigationView.visibility = View.GONE
                }
            }

//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }
}