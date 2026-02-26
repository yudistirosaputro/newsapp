package com.blank.feature.bookmark

import android.os.Bundle
import com.blank.core.base.BaseFragment
import com.blank.feature.bookmark.databinding.FragmentBookmarksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarksFragment : BaseFragment<FragmentBookmarksBinding>(FragmentBookmarksBinding::inflate) {

    override fun onViewReady(savedInstanceState: Bundle?) {
    }
}
