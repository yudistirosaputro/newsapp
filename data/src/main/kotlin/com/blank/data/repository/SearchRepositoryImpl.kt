package com.blank.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.blank.data.base.BasePagingSource
import com.blank.data.mapper.ArticleMapper
import com.blank.data.paging.SearchPagingSource
import com.blank.data.remote.api.NewsApiService
import com.blank.domain.model.ArticleModel
import com.blank.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService,
    private val articleMapper: ArticleMapper,
) : SearchRepository {

    override fun searchNews(query: String): Flow<PagingData<ArticleModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = BasePagingSource.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                SearchPagingSource(
                    newsApiService = newsApiService,
                    articleMapper = articleMapper,
                    query = query,
                )
            }
        ).flow
    }
}
