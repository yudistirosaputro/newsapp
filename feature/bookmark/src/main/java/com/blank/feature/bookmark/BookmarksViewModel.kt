package com.blank.feature.bookmark

import androidx.lifecycle.viewModelScope
import com.blank.core.base.BaseViewModel
import com.blank.core.base.UiState
import com.blank.core.model.NewsItem
import com.blank.domain.usecase.GetBookmarksUseCase
import com.blank.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    getBookmarksUseCase: GetBookmarksUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
) : BaseViewModel() {

    val bookmarks: StateFlow<UiState<List<NewsItem>>> =
        getBookmarksUseCase()
            .map { articles ->
                if (articles.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(
                        articles.map { article ->
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
                                isBookmarked = true,
                            )
                        }
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

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
}
