package com.blank.feature.explore

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.blank.core.base.BaseViewModel
import com.blank.core.model.NewsItem
import com.blank.domain.repository.ConnectivityObserver
import com.blank.domain.usecase.GetBookmarkedUrlsUseCase
import com.blank.domain.usecase.GetSearchNewsUseCase
import com.blank.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getSearchNewsUseCase: GetSearchNewsUseCase,
    private val getBookmarkedUrlsUseCase: GetBookmarkedUrlsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    connectivityObserver: ConnectivityObserver,
) : BaseViewModel() {

    val isOffline: StateFlow<Boolean> = connectivityObserver.isOnline
        .map { online -> !online }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchResults: Flow<PagingData<NewsItem>> = _query
        .debounce(DEBOUNCE_MS)
        .distinctUntilChanged()
        .filter { it.isNotEmpty() }
        .flatMapLatest { query ->
            getSearchNewsUseCase(query)
                .cachedIn(viewModelScope)
                .combine(getBookmarkedUrlsUseCase()) { pagingData, bookmarkedUrls ->
                    pagingData.map { article ->
                        NewsItem(
                            id = article.url,
                            title = article.title,
                            description = article.description,
                            content = article.content,
                            source = article.sourceName,
                            timeAgo = article.publishedAt,
                            category = "",
                            urlToImage = article.urlToImage,
                            url = article.url,
                            author = article.author,
                            isBookmarked = article.url in bookmarkedUrls,
                        )
                    }
                }
        }

    fun setQuery(query: String) {
        _query.value = query
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
        private const val DEBOUNCE_MS = 300L
    }
}
