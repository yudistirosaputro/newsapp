package com.blank.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val urlWithApiKey = originalRequest.url.newBuilder()
            .addQueryParameter("apiKey", apiKey)
            .build()
        val newRequest = originalRequest.newBuilder()
            .url(urlWithApiKey)
            .build()
        return chain.proceed(newRequest)
    }
}
