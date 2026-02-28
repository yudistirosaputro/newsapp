package com.blank.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewsItem(
    val id: String,
    val title: String,
    val description: String = "",
    val content: String = "",
    val source: String,
    val timeAgo: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val urlToImage: String = "",
    val url: String = "",
    val author: String = "",
) : Parcelable
