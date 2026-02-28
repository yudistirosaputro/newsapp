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
import androidx.core.net.toUri

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
        setupViews()
        setupClickListeners()
    }

    override fun observeState() {
        collectWithLifecycle(viewModel.isBookmarked) { isBookmarked ->
            updateBookmarkIcon(isBookmarked)
        }
    }

    private fun setupViews() {
        binding.apply {
            tvTitle.text = newsItem.title
            tvContent.text = newsItem.content
            tvAuthorName.text = newsItem.author
            tvMetaInfo.text = "${newsItem.source} • ${newsItem.timeAgo}"
            ivArticleImage.load(newsItem.urlToImage) {
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_placeholder)
                crossfade(true)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnBookmark.setOnClickListener {
            viewModel.toggleBookmark()
        }

        binding.btnShare.setOnClickListener {
            shareArticle()
        }

        binding.btnReadFullArticle.setOnClickListener {
            openFullArticle()
        }
    }

    private fun shareArticle() {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TITLE, newsItem.title)
            putExtra(android.content.Intent.EXTRA_TEXT, "${newsItem.title}\n\n${newsItem.url}")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    private fun openFullArticle() {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            data = newsItem.url.toUri()
        }
        startActivity(intent)
    }

    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        val iconRes = if (isBookmarked) {
            R.drawable.ic_bookmark_filled
        } else {
            R.drawable.ic_bookmark_outline
        }
        binding.btnBookmark.setImageResource(iconRes)
    }

    companion object {
        const val ARG_NEWS_ITEM = "newsItem"
    }
}
