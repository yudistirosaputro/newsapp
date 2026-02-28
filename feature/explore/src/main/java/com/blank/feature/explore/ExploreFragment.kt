package com.blank.feature.explore

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.blank.core.base.BaseFragment
import com.blank.core.extensions.collectWithLifecycle
import com.blank.core.extensions.gone
import com.blank.core.extensions.visible
import com.blank.core.model.NewsItem
import com.blank.feature.explore.adapter.SearchLoadStateAdapter
import com.blank.feature.explore.adapter.SearchResultAdapter
import com.blank.feature.explore.databinding.FragmentExploreBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class ExploreFragment : BaseFragment<FragmentExploreBinding>(FragmentExploreBinding::inflate) {

    private val viewModel: ExploreViewModel by viewModels()
    private lateinit var searchAdapter: SearchResultAdapter

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupAdapter()
        setupSearch()
    }

    override fun observeState() {
        collectWithLifecycle(viewModel.isOffline) { isOffline ->
            binding.tvOfflineBanner.visibility =
                if (isOffline) View.VISIBLE else View.GONE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collectLatest { pagingData ->
                searchAdapter.submitData(pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            searchAdapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .collectLatest { loadStates ->
                    handleLoadState(loadStates.refresh)
                }
        }
    }

    private fun setupAdapter() {
        searchAdapter = SearchResultAdapter(
            onItemClick = { newsItem -> navigateToDetail(newsItem) },
        )

        binding.rvSearchResults.apply {
            adapter = searchAdapter.withLoadStateFooter(
                footer = SearchLoadStateAdapter { searchAdapter.retry() },
            )
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearch() = with(binding) {
        etSearch.doAfterTextChanged { text ->
            val query = text?.toString().orEmpty()
            searchByQuery(query)
            btnClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE

            if (query.isEmpty()) {
                showEmptyState()
            }
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text?.toString().orEmpty()
                searchByQuery(query)
                true
            } else {
                false
            }
        }

        btnClear.setOnClickListener {
            etSearch.text?.clear()
            viewModel.setQuery("")
            showEmptyState()
        }

        btnClearSearch.setOnClickListener {
            etSearch.text?.clear()
            viewModel.setQuery("")
            showEmptyState()
        }
    }

    private fun searchByQuery(query: String) {
        when {
            viewModel.isOffline.value -> {
                handleOfflineView()
            }
            query.isNotEmpty() -> {
                viewModel.setQuery(query)
            }
        }
    }

    private fun handleOfflineView() = with(binding) {
        emptyStateView.gone()
        noResultsStateView.visible()
        tvNoResultsTitle.text = getString(R.string.offline_message)
        tvNoResultsSubtitle.text =
            getString(R.string.offline_search_subtitle)
        btnClearSearch.gone()
        btnBrowseTrending.gone()
    }
    private fun handleLoadState(refresh: LoadState) = with(binding) {
        val hasQuery = viewModel.query.value.isNotEmpty()

        when (refresh) {
            is LoadState.Loading -> {
                if (hasQuery) {
                    emptyStateView.gone()
                    noResultsStateView.gone()
                    rvSearchResults.gone()
                    tvResultsHeader.gone()
                    progressIndicator.visible()
                }
            }

            is LoadState.Error -> {
                progressIndicator.gone()
                val isOffline = refresh.error is IOException

                if (searchAdapter.itemCount > 0) {
                    showResultsState()
                } else {
                    rvSearchResults.gone()
                    tvResultsHeader.gone()

                    if (isOffline) {
                        handleOfflineView()
                    } else {
                        noResultsStateView.visible()
                        emptyStateView.gone()
                    }
                }
            }

            is LoadState.NotLoading -> {
                progressIndicator.gone()

                if (!hasQuery) {
                    showEmptyState()
                } else if (searchAdapter.itemCount == 0) {
                    showNoResultsState()
                } else {
                    showResultsState()
                }
            }
        }
    }

    private fun showEmptyState() = with(binding) {
        progressIndicator.gone()
        rvSearchResults.gone()
        tvResultsHeader.gone()
        noResultsStateView.gone()
        emptyStateView.visible()
    }

    private fun showNoResultsState() = with(binding) {
        rvSearchResults.gone()
        tvResultsHeader.gone()
        emptyStateView.gone()
        noResultsStateView.visible()
        tvNoResultsTitle.text = getString(R.string.no_results_title)
        tvNoResultsSubtitle.text = getString(R.string.no_results_subtitle)
        btnClearSearch.visible()
        btnBrowseTrending.gone()
    }

    private fun showResultsState() = with(binding) {
        emptyStateView.gone()
        noResultsStateView.gone()
        rvSearchResults.visible()

        val query = viewModel.query.value
        tvResultsHeader.text = getString(R.string.results_header, query)
        tvResultsHeader.visible()
    }

    private fun navigateToDetail(newsItem: NewsItem) {
        val bundle = Bundle().apply {
            putParcelable(ARG_NEWS_ITEM, newsItem)
        }
        findNavController().navigate(com.blank.core.R.id.detailArticleFragment, bundle)
    }

    companion object {
        private const val ARG_NEWS_ITEM = "newsItem"
    }
}
