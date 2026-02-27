package com.blank.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.blank.data.remote.api.NewsApiService
import com.blank.data.mapper.ArticleMapper
import com.blank.data.base.BasePagingSource
import com.blank.data.paging.NewsPagingSource
import com.blank.domain.model.ArticleModel
import com.blank.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService,
    private val articleMapper: ArticleMapper,
) : NewsRepository {

    override fun getTopHeadlines(
        country: String?,
        category: String?,
        query: String?,
    ): Flow<PagingData<ArticleModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = BasePagingSource.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                NewsPagingSource(
                    newsApiService = newsApiService,
                    articleMapper = articleMapper,
                    country = country,
                    category = category,
                    query = query,
                )
            }
        ).flow
    }
}
