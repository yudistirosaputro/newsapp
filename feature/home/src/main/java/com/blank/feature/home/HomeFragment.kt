package com.blank.feature.home

import android.os.Bundle
import com.blank.core.base.BaseFragment
import com.blank.feature.home.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun onViewReady(savedInstanceState: Bundle?) {
        // TODO: setup home screen
    }
}
