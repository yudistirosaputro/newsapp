package com.blank.feature.splash

import android.os.Bundle
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.blank.core.base.BaseFragment
import com.blank.feature.splash.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    override fun onViewReady(savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(SPLASH_DELAY)
            val request = NavDeepLinkRequest.Builder
                .fromUri(Uri.parse("app://newsapp/home"))
                .build()
            findNavController().navigate(request)
        }
    }

    companion object {
        private const val SPLASH_DELAY = 2000L
    }
}
