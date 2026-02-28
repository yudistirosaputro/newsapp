package com.blank.data.remote.api

import com.blank.data.remote.dto.ArticleDto
import com.blank.data.base.BaseResponse
import com.blank.data.base.ErrorResponse
import com.blank.data.helper.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String? = null,
        @Query("category") category: String? = null,
        @Query("q") query: String? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("page") page: Int? = null,
    ): NetworkResponse<BaseResponse<ArticleDto>, ErrorResponse>

    @GET("everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("pageSize") pageSize: Int? = null,
        @Query("page") page: Int? = null,
        @Query("language") language: String? = "en",
        @Query("sortBy") sortBy: String? = "publishedAt",
    ): NetworkResponse<BaseResponse<ArticleDto>, ErrorResponse>
}
