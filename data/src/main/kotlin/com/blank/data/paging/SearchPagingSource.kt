package com.blank.data.paging

import com.blank.data.base.BasePagingSource
import com.blank.data.base.BaseResponse
import com.blank.data.base.ErrorResponse
import com.blank.data.helper.NetworkResponse
import com.blank.data.mapper.ArticleMapper
import com.blank.data.remote.api.NewsApiService
import com.blank.data.remote.dto.ArticleDto
import com.blank.domain.model.ArticleModel

class SearchPagingSource(
    private val newsApiService: NewsApiService,
    private val articleMapper: ArticleMapper,
    private val query: String,
) : BasePagingSource<ArticleDto, ArticleModel>() {

    override suspend fun executeApi(
        page: Int,
        pageSize: Int,
    ): NetworkResponse<BaseResponse<ArticleDto>, ErrorResponse> {
        return newsApiService.getEverything(
            query = query,
            pageSize = pageSize,
            page = page,
        )
    }

    override fun mapItems(dtos: List<ArticleDto>): List<ArticleModel> =
        articleMapper.map(dtos)
}
