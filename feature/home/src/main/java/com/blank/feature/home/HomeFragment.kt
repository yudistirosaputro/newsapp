package com.blank.feature.home

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blank.core.base.BaseFragment
import com.blank.core.constants.NewsCategory
import com.blank.core.extensions.collectWithLifecycle
import com.blank.feature.home.adapter.BreakingNewsAdapter
import com.blank.feature.home.adapter.RecommendedNewsAdapter
import com.blank.feature.home.databinding.FragmentHomeBinding
import com.blank.feature.home.model.NewsItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var breakingNewsAdapter: BreakingNewsAdapter
    private lateinit var recommendedNewsAdapter: RecommendedNewsAdapter

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupAdapters()
        setupRecyclerViews()
        setupCategoryChips()
    }

    override fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.topHeadlines.collectLatest { pagingData ->
                    recommendedNewsAdapter.submitData(pagingData)
                }
            }
        }

        collectWithLifecycle(viewModel.isLoading) { isLoading ->
            // TODO: Show/hide loading indicator
        }

        collectWithLifecycle(viewModel.error) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAdapters() {
        breakingNewsAdapter = BreakingNewsAdapter(
            onItemClick = { newsItem ->
                navigateToDetail(newsItem)
            },
            onBookmarkClick = { newsItem ->
                viewModel.toggleBookmark(newsItem)
            }
        )

        recommendedNewsAdapter = RecommendedNewsAdapter(
            onItemClick = { newsItem ->
                navigateToDetail(newsItem)
            },
            onBookmarkClick = { newsItem ->
                viewModel.toggleBookmark(newsItem)
            }
        )

        recommendedNewsAdapter.addLoadStateListener { loadState ->
            val errorState = loadState.source.refresh as? LoadState.Error
                ?: loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
            errorState?.let {
                Toast.makeText(requireContext(), "Error: ${it.error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToDetail(newsItem: NewsItem) {
        val bundle = Bundle().apply {
            putParcelable(DetailArticleFragment.ARG_NEWS_ITEM, newsItem)
        }
        findNavController().navigate(R.id.detailArticleFragment, bundle)
    }

    private fun setupRecyclerViews() {
        binding.rvBreakingNews.apply {
            adapter = breakingNewsAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }

        binding.rvRecommendedNews.apply {
            adapter = recommendedNewsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupCategoryChips() {
        val chipCategoryMap = mapOf(
            R.id.chipAll to null,
            R.id.chipBusiness to NewsCategory.BUSINESS,
            R.id.chipEntertainment to NewsCategory.ENTERTAINMENT,
            R.id.chipGeneral to NewsCategory.GENERAL,
            R.id.chipHealth to NewsCategory.HEALTH,
            R.id.chipScience to NewsCategory.SCIENCE,
            R.id.chipSports to NewsCategory.SPORTS,
            R.id.chipTechnology to NewsCategory.TECHNOLOGY,
        )

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val category = chipCategoryMap[checkedIds[0]]
                viewModel.setCategory(category)
            }
        }
    }
}
