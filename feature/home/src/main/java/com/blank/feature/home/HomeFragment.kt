package com.blank.feature.home

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blank.core.base.BaseFragment
import com.blank.feature.home.adapter.BreakingNewsAdapter
import com.blank.feature.home.adapter.RecommendedNewsAdapter
import com.blank.feature.home.databinding.FragmentHomeBinding
import com.blank.feature.home.model.NewsItem
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private lateinit var breakingNewsAdapter: BreakingNewsAdapter
    private lateinit var recommendedNewsAdapter: RecommendedNewsAdapter

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupAdapters()
        setupRecyclerViews()
        setupCategoryChips()
        loadMockData()
    }

    private fun setupAdapters() {
        breakingNewsAdapter = BreakingNewsAdapter(
            onItemClick = { newsItem ->
                // TODO: Navigate to detail screen
                Toast.makeText(requireContext(), "Clicked: ${newsItem.title}", Toast.LENGTH_SHORT).show()
            },
            onBookmarkClick = { newsItem ->
                // TODO: Toggle bookmark
                Toast.makeText(requireContext(), "Bookmark: ${newsItem.title}", Toast.LENGTH_SHORT).show()
            }
        )

        recommendedNewsAdapter = RecommendedNewsAdapter(
            onItemClick = { newsItem ->
                Toast.makeText(requireContext(), "Clicked: ${newsItem.title}", Toast.LENGTH_SHORT).show()
            },
            onBookmarkClick = { newsItem ->
                Toast.makeText(requireContext(), "Bookmark: ${newsItem.title}", Toast.LENGTH_SHORT).show()
            }
        )
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
            setHasFixedSize(true)
        }
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds[0])
                val category = selectedChip?.text?.toString() ?: "All"
                Toast.makeText(requireContext(), "Category: $category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMockData() {
        val breakingNews = listOf(
            NewsItem(
                id = "1",
                title = "The Future of AI: What to Expect in 2025",
                source = "TechCrunch",
                timeAgo = "2 hours ago",
                category = "Technology"
            ),
            NewsItem(
                id = "2",
                title = "Global Markets React to New Economic Policies",
                source = "Reuters",
                timeAgo = "3 hours ago",
                category = "Business"
            ),
            NewsItem(
                id = "3",
                title = "Revolutionary Battery Technology Unveiled",
                source = "BBC News",
                timeAgo = "4 hours ago",
                category = "Science"
            ),
            NewsItem(
                id = "4",
                title = "Championship Finals: Everything You Need to Know",
                source = "ESPN",
                timeAgo = "5 hours ago",
                category = "Sports"
            )
        )

        val recommendedNews = listOf(
            NewsItem(
                id = "5",
                title = "SpaceX Successfully Launches New Satellite Constellation",
                source = "Space.com",
                timeAgo = "1 hour ago",
                category = "Technology"
            ),
            NewsItem(
                id = "6",
                title = "New Study Reveals Benefits of Mediterranean Diet",
                source = "Health Daily",
                timeAgo = "2 hours ago",
                category = "Health"
            ),
            NewsItem(
                id = "7",
                title = "Tech Giants Report Strong Quarterly Earnings",
                source = "CNBC",
                timeAgo = "3 hours ago",
                category = "Business"
            ),
            NewsItem(
                id = "8",
                title = "Climate Summit Reaches Historic Agreement",
                source = "BBC News",
                timeAgo = "4 hours ago",
                category = "Science"
            ),
            NewsItem(
                id = "9",
                title = "Olympic Preparations Enter Final Stage",
                source = "Sports Illustrated",
                timeAgo = "5 hours ago",
                category = "Sports"
            ),
            NewsItem(
                id = "10",
                title = "Breakthrough in Cancer Research Announced",
                source = "Medical News",
                timeAgo = "6 hours ago",
                category = "Health"
            )
        )

        breakingNewsAdapter.submitList(breakingNews)
        recommendedNewsAdapter.submitList(recommendedNews)
    }
}
