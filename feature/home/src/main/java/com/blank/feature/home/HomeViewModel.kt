package com.blank.feature.home

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.blank.core.base.BaseViewModel
import com.blank.domain.model.ArticleModel
import com.blank.domain.usecase.GetBookmarkedUrlsUseCase
import com.blank.domain.usecase.GetTopHeadlinesUseCase
import com.blank.domain.usecase.ToggleBookmarkUseCase
import com.blank.feature.home.model.NewsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val getBookmarkedUrlsUseCase: GetBookmarkedUrlsUseCase,
) : BaseViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val bookmarkedUrls: Flow<Set<String>> = getBookmarkedUrlsUseCase()

    @OptIn(ExperimentalCoroutinesApi::class)
    val topHeadlines: Flow<PagingData<NewsItem>> = _selectedCategory
        .flatMapLatest { category ->
            getTopHeadlinesUseCase(
                country = COUNTRY,
                category = category,
            )
        }
        .cachedIn(viewModelScope)
        .combine(bookmarkedUrls) { pagingData, urls ->
            pagingData.map { article -> article.toNewsItem(article.url in urls) }
        }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleBookmark(newsItem: NewsItem) {
        viewModelScope.launch {
            val article = newsItem.toArticleModel()
            toggleBookmarkUseCase(article)
        }
    }

    private fun ArticleModel.toNewsItem(isBookmarked: Boolean = false): NewsItem {
        return NewsItem(
            id = url,
            title = title,
            description = description,
            content = content,
            source = sourceName,
            timeAgo = publishedAt,
            category = "",
            urlToImage = urlToImage,
            url = url,
            author = author,
            isBookmarked = isBookmarked,
        )
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

    companion object {
        const val COUNTRY = "us"
    }
}
