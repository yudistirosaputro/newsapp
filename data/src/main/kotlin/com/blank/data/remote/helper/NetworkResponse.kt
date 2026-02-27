package com.blank.data.remote.helper

import java.io.IOException

sealed class NetworkResponse<out S : Any, out E : Any> {
    data class Success<S : Any>(val data: S) : NetworkResponse<S, Nothing>()
    data class ErrorApi<E : Any>(val code: Int, val error: E) : NetworkResponse<Nothing, E>()
    data class ErrorNetwork(val error: IOException) : NetworkResponse<Nothing, Nothing>()
    data class ErrorUnknown(val error: Throwable?) : NetworkResponse<Nothing, Nothing>()
}
