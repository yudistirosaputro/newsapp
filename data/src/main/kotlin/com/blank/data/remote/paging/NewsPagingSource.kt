package com.blank.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.blank.data.remote.api.NewsApiService
import com.blank.data.mapper.ArticleMapper
import com.blank.data.remote.helper.NetworkResponse
import com.blank.domain.model.ArticleModel

class NewsPagingSource(
    private val newsApiService: NewsApiService,
    private val articleMapper: ArticleMapper,
    private val country: String?,
    private val category: String?,
    private val query: String?,
) : PagingSource<Int, ArticleModel>() {

    companion object {
        private const val STARTING_PAGE = 1
        const val PAGE_SIZE = 20
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleModel> {
        val page = params.key ?: STARTING_PAGE
        return when (
            val response = newsApiService.getTopHeadlines(
                country = country,
                category = category,
                query = query,
                pageSize = params.loadSize,
                page = page,
            )
        ) {
            is NetworkResponse.Success -> {
                val body = response.data
                val articles = articleMapper.map(body.articles)
                val totalResults = body.totalResults

                val nextKey = if (articles.isEmpty() || page * params.loadSize >= totalResults) {
                    null
                } else {
                    page + 1
                }

                LoadResult.Page(
                    data = articles,
                    prevKey = if (page == STARTING_PAGE) null else page - 1,
                    nextKey = nextKey,
                )
            }

            is NetworkResponse.ErrorApi -> {
                LoadResult.Error(
                    Exception("API Error ${response.code}: ${response.error.message}")
                )
            }

            is NetworkResponse.ErrorNetwork -> {
                LoadResult.Error(response.error)
            }

            is NetworkResponse.ErrorUnknown -> {
                LoadResult.Error(
                    response.error ?: Exception("Unknown error occurred")
                )
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ArticleModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
