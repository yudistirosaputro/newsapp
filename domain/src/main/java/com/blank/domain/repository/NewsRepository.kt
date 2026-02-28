package com.blank.domain.repository

import androidx.paging.PagingData
import com.blank.domain.base.Resource
import com.blank.domain.model.ArticleModel
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    fun getTopHeadlinesPaged(
        country: String,
        category: String,
        onOfflineFallback: () -> Unit = {},
    ): Flow<PagingData<ArticleModel>>
}
