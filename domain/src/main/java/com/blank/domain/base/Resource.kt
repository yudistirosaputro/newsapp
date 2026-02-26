package com.blank.domain.base

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    data class Exception(val throwable: Throwable) : Resource<Nothing>()
}
