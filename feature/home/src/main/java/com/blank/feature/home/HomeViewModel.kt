package com.blank.feature.home

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.blank.core.base.BaseViewModel
import com.blank.domain.model.ArticleModel
import com.blank.domain.usecase.GetTopHeadlinesUseCase
import com.blank.feature.home.model.NewsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase,
) : BaseViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val topHeadlines: Flow<PagingData<NewsItem>> = _selectedCategory
        .flatMapLatest { category ->
            getTopHeadlinesUseCase(
                country = COUNTRY,
                category = category,
            ).map { pagingData ->
                pagingData.map { article -> article.toNewsItem() }
            }
        }
        .cachedIn(viewModelScope)

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    private fun ArticleModel.toNewsItem(): NewsItem {
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
        )
    }
    companion object {
        const val COUNTRY = "us"
    }
}
