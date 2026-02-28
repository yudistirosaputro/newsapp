package com.blank.feature.home

import androidx.lifecycle.viewModelScope
import com.blank.core.base.BaseViewModel
import com.blank.domain.model.ArticleModel
import com.blank.domain.usecase.ToggleBookmarkUseCase
import com.blank.domain.repository.BookmarkRepository
import com.blank.core.model.NewsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailArticleViewModel @Inject constructor(
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val bookmarkRepository: BookmarkRepository,
) : BaseViewModel() {

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private var currentArticle: ArticleModel? = null

    fun setArticle(newsItem: NewsItem) {
        currentArticle = newsItem.toArticleModel()
        viewModelScope.launch {
            _isBookmarked.value = bookmarkRepository.isBookmarked(newsItem.url)
        }
    }

    fun toggleBookmark() {
        val article = currentArticle ?: return
        viewModelScope.launch {
            toggleBookmarkUseCase(article)
            _isBookmarked.value = bookmarkRepository.isBookmarked(article.url)
        }
    }

    private fun NewsItem.toArticleModel(): ArticleModel {
        return ArticleModel(
            sourceId = "",
            sourceName = source,
            author = author,
            title = title,
            description = description,
            url = url,
            urlToImage = urlToImage,
            publishedAt = timeAgo,
            content = content,
            isBookmarked = isBookmarked,
        )
    }
}
