package com.blank.domain.repository

import com.blank.domain.base.Resource
import com.blank.domain.model.ArticleModel
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getAllBookmarks(): Flow<List<ArticleModel>>
    fun getBookmarkedUrls(): Flow<Set<String>>
    suspend fun toggleBookmark(article: ArticleModel): Resource<Unit>
    suspend fun isBookmarked(url: String): Boolean
}
