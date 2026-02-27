package com.blank.domain.repository

import androidx.paging.PagingData
import com.blank.domain.model.ArticleModel
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    fun getTopHeadlines(
        country: String? = "us",
        category: String? = null,
        query: String? = null,
    ): Flow<PagingData<ArticleModel>>
}
