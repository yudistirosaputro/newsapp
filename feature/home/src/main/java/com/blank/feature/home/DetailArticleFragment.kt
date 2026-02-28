package com.blank.feature.home

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.blank.core.base.BaseFragment
import com.blank.core.extensions.collectWithLifecycle
import com.blank.feature.home.databinding.FragmentDetailArticleBinding
import com.blank.core.model.NewsItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailArticleFragment :
    BaseFragment<FragmentDetailArticleBinding>(FragmentDetailArticleBinding::inflate) {

    private val viewModel: DetailArticleViewModel by viewModels()

    private lateinit var newsItem: NewsItem

    override fun onViewReady(savedInstanceState: Bundle?) {
        newsItem = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_NEWS_ITEM, NewsItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_NEWS_ITEM)
        } ?: throw IllegalArgumentException("NewsItem argument is required")

        viewModel.setArticle(newsItem)
        setupToolbar()
        setupViews()
        setupClickListeners()
    }

    override fun observeState() {
        collectWithLifecycle(viewModel.isBookmarked) { isBookmarked ->
            updateBookmarkIcon(isBookmarked)
        }
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
            ivArticleImage.load(newsItem.urlToImage) {
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_placeholder)
                crossfade(true)
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabBookmark.setOnClickListener {
            viewModel.toggleBookmark()
        }
    }

    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        val iconRes = if (isBookmarked) {
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
