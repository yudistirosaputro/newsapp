package com.blank.data.paging

import com.blank.data.base.BaseOfflineFirstPagingSource
import com.blank.data.base.BaseResponse
import com.blank.data.base.ErrorResponse
import com.blank.data.helper.NetworkResponse
import com.blank.data.local.dao.ArticleDao
import com.blank.data.local.mapper.ArticleEntityMapper
import com.blank.data.local.mapper.toEntity
import com.blank.data.mapper.ArticleMapper
import com.blank.data.remote.api.NewsApiService
import com.blank.data.remote.dto.ArticleDto
import com.blank.domain.model.ArticleModel

class TopHeadlinesCachingPagingSource(
    private val newsApiService: NewsApiService,
    private val articleDao: ArticleDao,
    private val articleMapper: ArticleMapper,
    private val articleEntityMapper: ArticleEntityMapper,
    private val country: String,
    private val category: String,
    offlineCallback: (() -> Unit)? = null,
) : BaseOfflineFirstPagingSource<ArticleDto, ArticleModel>(offlineCallback) {

    override suspend fun executeApi(
        page: Int,
        pageSize: Int,
    ): NetworkResponse<BaseResponse<ArticleDto>, ErrorResponse> {
        return newsApiService.getTopHeadlines(
            country = country,
            category = category,
            pageSize = pageSize,
            page = page,
        )
    }

    override fun mapItems(dtos: List<ArticleDto>): List<ArticleModel> {
        return articleMapper.map(dtos)
    }

    override suspend fun cacheItems(items: List<ArticleModel>) {
        val entities = items
            .filter { it.url.isNotBlank() }
            .map { article ->
                val existing = articleDao.getArticleByUrl(article.url)
                article.toEntity(
                    isTopHeadline = true,
                    isBookmarked = existing?.isBookmarked ?: false,
                )
            }
        articleDao.upsertAll(entities)
    }

    override suspend fun loadCachedItems(offset: Int, limit: Int): List<ArticleModel> {
        return articleDao.getTopHeadlinesPage(offset, limit)
            .map { entity -> articleEntityMapper.mapItem(entity) }
    }

    override suspend fun clearCache() {
        articleDao.deleteNonBookmarkedTopHeadlines()
    }
}
