package com.blank.feature.explore

import android.os.Bundle
import com.blank.core.base.BaseFragment
import com.blank.feature.explore.databinding.FragmentExploreBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExploreFragment : BaseFragment<FragmentExploreBinding>(FragmentExploreBinding::inflate) {

    override fun onViewReady(savedInstanceState: Bundle?) {
    }
}
