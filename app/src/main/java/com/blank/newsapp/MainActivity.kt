package com.blank.newsapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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
        setupBottomNavigation()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            // Apply bottom padding to the container to handle system navigation bar
            binding.bottomNavigationContainer.updatePadding(bottom = navigationBars.bottom)
            
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
                R.id.splashFragment, com.blank.core.R.id.detailArticleFragment -> {
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
