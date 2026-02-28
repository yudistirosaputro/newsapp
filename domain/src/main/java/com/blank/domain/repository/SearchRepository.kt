package com.blank.domain.repository

import androidx.paging.PagingData
import com.blank.domain.model.ArticleModel
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchNews(query: String): Flow<PagingData<ArticleModel>>
}
