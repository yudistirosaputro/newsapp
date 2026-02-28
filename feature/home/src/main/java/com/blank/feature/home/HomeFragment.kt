package com.blank.feature.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blank.core.extensions.navigateWithParcelable
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.blank.core.base.BaseFragment
import com.blank.core.extensions.collectWithLifecycle
import com.blank.core.extensions.gone
import com.blank.core.extensions.visible
import com.blank.feature.home.adapter.NewsLoadStateAdapter
import com.blank.feature.home.adapter.RecommendedNewsAdapter
import com.blank.feature.home.databinding.FragmentHomeBinding
import com.blank.core.model.NewsItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var recommendedNewsAdapter: RecommendedNewsAdapter

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupAdapters()
        setupRecyclerViews()
        setupSwipeRefresh()
    }

    override fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topHeadlines.collectLatest { pagingData ->
                recommendedNewsAdapter.submitData(pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            recommendedNewsAdapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .collectLatest { loadStates ->
                    val refresh = loadStates.refresh
                    handleRefreshState(refresh)
                }
        }

        collectWithLifecycle(viewModel.isOffline) { isOffline ->
            binding.tvOfflineBanner.visibility =
                if (isOffline) View.VISIBLE else View.GONE
        }
    }

    private fun handleRefreshState(refresh: LoadState) = with(binding) {
        swipeRefreshLayout.isRefreshing = refresh is LoadState.Loading && recommendedNewsAdapter.itemCount > 0

        when (refresh) {
            is LoadState.Loading -> {
                if (recommendedNewsAdapter.itemCount == 0) {
                    progressIndicator.visible()
                    recommendedNewsSection.gone()
                    tvEmptyState.gone()
                } else {
                    progressIndicator.gone()
                }
            }

            is LoadState.Error -> {
                progressIndicator.gone()
                val isOffline = refresh.error is IOException

                if (recommendedNewsAdapter.itemCount > 0) {
                    recommendedNewsSection.visible()
                    tvEmptyState.gone()
                    if (isOffline) tvOfflineBanner.visible()
                } else {
                    recommendedNewsSection.gone()
                    tvEmptyState.visible()
                    tvEmptyState.text = if (isOffline) {
                        getString(R.string.offline_message)
                    } else {
                        refresh.error.message ?: getString(R.string.no_articles)
                    }
                }
            }

            is LoadState.NotLoading -> {
                progressIndicator.gone()

                if (recommendedNewsAdapter.itemCount == 0) {
                    recommendedNewsSection.gone()
                    tvEmptyState.visible()
                    tvEmptyState.text = getString(R.string.no_articles)
                } else {
                    recommendedNewsSection.visible()
                    tvEmptyState.gone()
                }
            }
        }
    }

    private fun setupAdapters() {
        recommendedNewsAdapter = RecommendedNewsAdapter(
            onItemClick = { newsItem -> navigateToDetail(newsItem) },
            onBookmarkClick = { newsItem -> viewModel.toggleBookmark(newsItem) }
        )
    }

    private fun navigateToDetail(newsItem: NewsItem) {
        findNavController().navigateWithParcelable(
            destinationId = com.blank.core.R.id.detailArticleFragment,
            key = DetailArticleFragment.ARG_NEWS_ITEM,
            value = newsItem,
        )
    }

    private fun setupRecyclerViews() {
        binding.rvRecommendedNews.apply {
            // Combine main adapter with LoadStateAdapter for pagination loading
            adapter = recommendedNewsAdapter.withLoadStateFooter(
                footer = NewsLoadStateAdapter { recommendedNewsAdapter.retry() }
            )
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (viewModel.isOffline.value) {
                binding.swipeRefreshLayout.isRefreshing = false
            } else {
                recommendedNewsAdapter.refresh()
            }
        }
    }
}
