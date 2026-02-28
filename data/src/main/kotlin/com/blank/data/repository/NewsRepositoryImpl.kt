package com.blank.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.blank.data.base.BaseOfflineFirstPagingSource
import com.blank.data.local.dao.ArticleDao
import com.blank.data.local.mapper.ArticleEntityMapper
import com.blank.data.mapper.ArticleMapper
import com.blank.data.paging.TopHeadlinesCachingPagingSource
import com.blank.data.remote.api.NewsApiService
import com.blank.domain.model.ArticleModel
import com.blank.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService,
    private val articleMapper: ArticleMapper,
    private val articleDao: ArticleDao,
    private val articleEntityMapper: ArticleEntityMapper,
) : NewsRepository {

    override fun getTopHeadlinesPaged(
        country: String,
        category: String,
        onOfflineFallback: () -> Unit,
    ): Flow<PagingData<ArticleModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = BaseOfflineFirstPagingSource.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                TopHeadlinesCachingPagingSource(
                    newsApiService = newsApiService,
                    articleDao = articleDao,
                    articleMapper = articleMapper,
                    articleEntityMapper = articleEntityMapper,
                    country = country,
                    category = category,
                    offlineCallback = onOfflineFallback,
                )
            }
        ).flow
    }
}
