package com.blank.feature.bookmark

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.blank.core.extensions.navigateWithParcelable
import androidx.recyclerview.widget.LinearLayoutManager
import com.blank.core.base.BaseFragment
import com.blank.core.base.UiState
import com.blank.core.extensions.collectWithLifecycle
import com.blank.core.extensions.gone
import com.blank.core.extensions.visible
import com.blank.core.model.NewsItem
import com.blank.feature.bookmark.databinding.FragmentBookmarksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarksFragment : BaseFragment<FragmentBookmarksBinding>(FragmentBookmarksBinding::inflate) {

    private val viewModel: BookmarksViewModel by viewModels()
    private lateinit var bookmarkAdapter: BookmarkAdapter

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupAdapter()
        setupRecyclerView()
    }
    override fun observeState() = with(binding) {
        collectWithLifecycle(viewModel.bookmarks) { state ->
            when (state) {
                is UiState.Loading -> {
                   rvBookmarks.gone()
                    emptyState.gone()
                }

                is UiState.Success -> {
                   rvBookmarks.visible()
                    emptyState.gone()
                    bookmarkAdapter.submitList(state.data)
                }

                is UiState.Empty -> {
                    rvBookmarks.gone()
                    emptyState.visible()
                }

                is UiState.Error -> {
                    rvBookmarks.gone()
                    emptyState.visible()
                }
            }
        }
    }

    private fun setupAdapter() {
        bookmarkAdapter = BookmarkAdapter(
            onItemClick = { newsItem -> navigateToDetail(newsItem) },
            onBookmarkClick = { newsItem -> viewModel.toggleBookmark(newsItem) },
        )
    }

    private fun navigateToDetail(newsItem: NewsItem) {
        findNavController().navigateWithParcelable(
            destinationId = com.blank.core.R.id.detailArticleFragment,
            key = ARG_NEWS_ITEM,
            value = newsItem,
        )
    }

    private fun setupRecyclerView() {
        binding.rvBookmarks.apply {
            adapter = bookmarkAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    companion object {
        private const val ARG_NEWS_ITEM = "newsItem"
    }
}
