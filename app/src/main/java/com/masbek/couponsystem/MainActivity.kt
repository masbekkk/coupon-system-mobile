package com.masbek.couponsystem

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.masbek.couponsystem.databinding.ActivityMainBinding
import com.masbek.couponsystem.util.AuthInterceptor
import com.masbek.couponsystem.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var sessionManager: SessionManager

    private val unauthorizedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            navigateToLogin()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showBottomNav = destination.id in listOf(
                R.id.dashboardFragment,
                R.id.projectsFragment,
                R.id.settingsFragment
            )
            binding.bottomNav.visibility = if (showBottomNav) View.VISIBLE else View.GONE
        }

        if (sessionManager.isLoggedIn()) {
            navController.navigate(R.id.dashboardFragment)
        }

        ContextCompat.registerReceiver(
            this,
            unauthorizedReceiver,
            IntentFilter(AuthInterceptor.ACTION_UNAUTHORIZED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(unauthorizedReceiver)
        } catch (_: Exception) {}
    }

    private fun navigateToLogin() {
        runOnUiThread {
            navController.navigate(R.id.loginFragment)
        }
    }

    private fun applyTheme() {
        when (sessionManager.getTheme()) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
