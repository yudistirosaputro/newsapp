package com.blank.newsapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.blank.core.base.BaseActivity
import com.blank.newsapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupWindowInsets()
        setupBottomNavigation()
    }

    private fun setupWindowInsets() {
        // Edge-to-edge is enabled, let each fragment handle insets via fitsSystemWindows
        // This listener prevents the default consumption of insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            // Don't consume insets at the root level, let children handle them
            insets
        }
    }

    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.detailArticleFragment -> {
                    binding.bottomNavigationContainer.visibility = View.GONE
                    // Extend fragment to bottom when bottom nav is hidden
                    (binding.navHostFragment.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
                        bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                        bottomToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                    }
                }
                else -> {
                    binding.bottomNavigationContainer.visibility = View.VISIBLE
                    // Constrain fragment above bottom nav
                    (binding.navHostFragment.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
                        bottomToTop = R.id.bottomNavigationContainer
                        bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                    }
                }
            }
            binding.navHostFragment.requestLayout()
        }
    }
}
