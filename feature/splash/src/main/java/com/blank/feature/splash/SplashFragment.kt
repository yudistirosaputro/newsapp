package com.blank.feature.splash

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blank.core.base.BaseFragment
import com.blank.core.extensions.navigateDeepLink
import com.blank.feature.splash.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    override fun onViewReady(savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(SPLASH_DELAY)
            findNavController().navigateDeepLink("app://newsapp/home")
        }
    }

    companion object {
        private const val SPLASH_DELAY = 2000L
    }
}
