package com.blank.feature.home.model

data class NewsItem(
    val id: String,
    val title: String,
    val source: String,
    val timeAgo: String,
    val category: String,
    val isBookmarked: Boolean = false,
)
