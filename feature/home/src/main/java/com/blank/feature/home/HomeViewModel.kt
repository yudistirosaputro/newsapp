package com.blank.feature.home

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.blank.core.base.BaseViewModel
import com.blank.core.model.NewsItem
import com.blank.core.util.TimeUtil
import com.blank.domain.repository.ConnectivityObserver
import com.blank.domain.usecase.GetBookmarkedUrlsUseCase
import com.blank.domain.usecase.GetTopHeadlinesUseCase
import com.blank.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getTopHeadlinesUseCase: GetTopHeadlinesUseCase,
    getBookmarkedUrlsUseCase: GetBookmarkedUrlsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    connectivityObserver: ConnectivityObserver,
) : BaseViewModel() {

    val isOffline: StateFlow<Boolean> = connectivityObserver.isOnline
        .map { online -> !online }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val topHeadlines: Flow<PagingData<NewsItem>> =
        getTopHeadlinesUseCase(
            country = COUNTRY,
            category = CATEGORY,
            onOfflineFallback = { /* handled by ConnectivityObserver */ },
        )
            .cachedIn(viewModelScope)
            .combine(getBookmarkedUrlsUseCase()) { pagingData, bookmarkedUrls ->
                pagingData.map { article ->
                    NewsItem(
                        id = article.url,
                        title = article.title,
                        description = article.description,
                        content = article.content,
                        source = article.sourceName,
                        timeAgo = TimeUtil.fromIsoString(article.publishedAt),
                        category = CATEGORY,
                        urlToImage = article.urlToImage,
                        url = article.url,
                        author = article.author,
                        isBookmarked = article.url in bookmarkedUrls,
                    )
                }
            }

    fun toggleBookmark(newsItem: NewsItem) {
        viewModelScope.launch {
            toggleBookmarkUseCase(newsItem.toArticleModel())
        }
    }

    private fun NewsItem.toArticleModel() = com.blank.domain.model.ArticleModel(
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

    companion object {
        private const val COUNTRY = "us"
        private const val CATEGORY = "technology"
    }
}
