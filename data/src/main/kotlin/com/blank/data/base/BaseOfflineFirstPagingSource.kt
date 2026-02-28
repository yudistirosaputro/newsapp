package com.blank.data.base

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.blank.data.helper.NetworkResponse

/**
 * Generic PagingSource with offline-first caching strategy.
 *
 * All fetched pages are cached locally. On network failure, falls back
 * to local cache for any previously loaded page.
 *
 * Subclasses provide:
 * - [executeApi]       — the Retrofit call for a given page/size
 * - [mapItems]         — DTO list → domain model list
 * - [cacheItems]       — persist items locally (e.g. Room upsert)
 * - [loadCachedItems]  — read cached items by offset/limit
 * - [clearCache]       — clear stale cache on refresh
 */
abstract class BaseOfflineFirstPagingSource<DTO : Any, Model : Any>(
    private val offlineCallback: (() -> Unit)? = null,
) : PagingSource<Int, Model>() {

    abstract suspend fun executeApi(
        page: Int,
        pageSize: Int,
    ): NetworkResponse<BaseResponse<DTO>, ErrorResponse>

    abstract fun mapItems(dtos: List<DTO>): List<Model>

    abstract suspend fun cacheItems(items: List<Model>)

    abstract suspend fun loadCachedItems(offset: Int, limit: Int): List<Model>

    abstract suspend fun clearCache()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Model> {
        val page = params.key ?: STARTING_PAGE

        return when (val response = executeApi(page, PAGE_SIZE)) {
            is NetworkResponse.Success -> handleSuccess(response.data, page)

            is NetworkResponse.ErrorNetwork -> handleOfflineFallback(page, response.error)

            is NetworkResponse.ErrorApi -> LoadResult.Error(
                Exception("API Error ${response.code}: ${response.error.message}")
            )

            is NetworkResponse.ErrorUnknown -> LoadResult.Error(
                response.error ?: Exception("Unknown error occurred")
            )
        }
    }

    private suspend fun handleSuccess(
        body: BaseResponse<DTO>,
        page: Int,
    ): LoadResult<Int, Model> {
        val items = mapItems(body.articles)

        if (page == STARTING_PAGE) clearCache()
        cacheItems(items)

        val totalResults = body.totalResults
        val nextKey = if (items.isEmpty() || page * PAGE_SIZE >= totalResults) {
            null
        } else {
            page + 1
        }

        return LoadResult.Page(
            data = items,
            prevKey = if (page == STARTING_PAGE) null else page - 1,
            nextKey = nextKey,
        )
    }

    private suspend fun handleOfflineFallback(
        page: Int,
        error: Exception,
    ): LoadResult<Int, Model> {
        val offset = (page - STARTING_PAGE) * PAGE_SIZE
        val cached = loadCachedItems(offset, PAGE_SIZE)

        return if (cached.isNotEmpty()) {
            offlineCallback?.invoke()
            val nextKey = if (cached.size < PAGE_SIZE) {
                null
            } else {
                page + 1
            }
            LoadResult.Page(
                data = cached,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = nextKey,
            )
        } else {
            LoadResult.Error(error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Model>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    companion object {
        const val STARTING_PAGE = 1
        const val PAGE_SIZE = 5
    }
}
