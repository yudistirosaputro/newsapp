package com.blank.data.base

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.blank.data.helper.NetworkResponse

/**
 * Generic PagingSource that handles [com.blank.data.helper.NetworkResponse] boilerplate.
 *
 * Subclasses only provide:
 * - [executeApi] — the Retrofit call for a given page/size
 * - [mapItems]  — DTO list → domain model list
 */
abstract class BasePagingSource<DTO : Any, Model : Any> : PagingSource<Int, Model>() {

    companion object {
        const val STARTING_PAGE = 1
        const val PAGE_SIZE = 5
    }

    abstract suspend fun executeApi(
        page: Int,
        pageSize: Int,
    ): NetworkResponse<BaseResponse<DTO>, ErrorResponse>

    abstract fun mapItems(dtos: List<DTO>): List<Model>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Model> {
        val page = params.key ?: STARTING_PAGE
        return when (val response = executeApi(page, params.loadSize)) {
            is NetworkResponse.Success -> {
                val body = response.data
                val items = mapItems(body.articles)
                val totalResults = body.totalResults

                val nextKey = if (items.isEmpty() || page * params.loadSize >= totalResults) {
                    null
                } else {
                    page + 1
                }

                LoadResult.Page(
                    data = items,
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

    override fun getRefreshKey(state: PagingState<Int, Model>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}