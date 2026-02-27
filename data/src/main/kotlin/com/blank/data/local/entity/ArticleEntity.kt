package com.blank.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String,
    val sourceId: String,
    val sourceName: String,
    val author: String,
    val title: String,
    val description: String,
    val urlToImage: String,
    val publishedAt: String,
    val content: String,
    val isBookmarked: Boolean = false,
    val isTopHeadline: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis(),
)
