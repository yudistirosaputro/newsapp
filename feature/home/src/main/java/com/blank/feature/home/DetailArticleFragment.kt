package com.blank.feature.home

import android.os.Bundle
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.blank.core.base.BaseFragment
import com.blank.feature.home.databinding.FragmentDetailArticleBinding
import com.blank.feature.home.model.NewsItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailArticleFragment :
    BaseFragment<FragmentDetailArticleBinding>(FragmentDetailArticleBinding::inflate) {

    private lateinit var newsItem: NewsItem

    override fun onViewReady(savedInstanceState: Bundle?) {
        newsItem = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_NEWS_ITEM, NewsItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_NEWS_ITEM)
        } ?: throw IllegalArgumentException("NewsItem argument is required")
        
        setupToolbar()
        setupViews()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            collapsingToolbar.title = newsItem.source
        }
    }

    private fun setupViews() {
        binding.apply {
            tvTitle.text = newsItem.title
            tvDescription.text = newsItem.description
            tvContent.text = newsItem.content
            tvSource.text = newsItem.source
            tvTime.text = newsItem.timeAgo
            chipCategory.text = newsItem.category
            ivArticleImage.setImageResource(R.drawable.ic_placeholder)
            updateBookmarkIcon()
        }
    }

    private fun setupClickListeners() {
        binding.fabBookmark.setOnClickListener {
            toggleBookmark()
        }
    }

    private fun toggleBookmark() {
        newsItem = newsItem.copy(isBookmarked = !newsItem.isBookmarked)
        updateBookmarkIcon()

        val message = if (newsItem.isBookmarked) {
            "Added to bookmarks"
        } else {
            "Removed from bookmarks"
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun updateBookmarkIcon() {
        val iconRes = if (newsItem.isBookmarked) {
            R.drawable.ic_bookmark_filled
        } else {
            R.drawable.ic_bookmark_outline
        }
        binding.fabBookmark.setImageResource(iconRes)
    }

    companion object {
        const val ARG_NEWS_ITEM = "newsItem"
    }
}
