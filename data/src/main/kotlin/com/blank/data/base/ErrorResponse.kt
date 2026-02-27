package com.blank.data.base

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("code")
    val code: String?,
    @SerializedName("message")
    val message: String?,
)
