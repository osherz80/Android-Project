package com.colProj.bookshare

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.colProj.bookshare.databinding.ActivityMainBinding
import com.colProj.bookshare.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Only apply top padding for status bar to the root
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Check if user is already logged in (Firebase or Local)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        AuthRepository.getInstance(this).hasLoggedInUser { isLocalLoggedIn ->
            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

            if (firebaseUser != null || isLocalLoggedIn) {
                navGraph.setStartDestination(R.id.homeFragment)
            } else {
                navGraph.setStartDestination(R.id.authFragment)
            }
            navController.graph = navGraph
        }

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val screensWithNav = setOf(
                R.id.homeFragment,
                R.id.searchFragment,
                R.id.addBookFragment,
                R.id.profileFragment
            )

            if (destination.id in screensWithNav) {
                binding.bottomNavigation.visibility = View.VISIBLE
            } else {
                binding.bottomNavigation.visibility = View.GONE
            }
        }
    }
}