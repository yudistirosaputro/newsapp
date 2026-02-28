package com.blank.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.blank.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles WHERE isTopHeadline = 1 ORDER BY cachedAt DESC")
    fun getTopHeadlines(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isTopHeadline = 1 ORDER BY cachedAt DESC")
    fun getTopHeadlinesPaging(): PagingSource<Int, ArticleEntity>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles WHERE isTopHeadline = 1 AND isBookmarked = 0")
    suspend fun deleteNonBookmarkedTopHeadlines()

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE url = :url")
    suspend fun updateBookmarkStatus(url: String, isBookmarked: Boolean)

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY cachedAt DESC")
    fun getBookmarkedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE url = :url LIMIT 1")
    suspend fun getArticleByUrl(url: String): ArticleEntity?

    @Query("SELECT url FROM articles WHERE isBookmarked = 1")
    fun getBookmarkedUrls(): Flow<List<String>>

    @Query("SELECT * FROM articles WHERE isTopHeadline = 1 ORDER BY cachedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getTopHeadlinesPage(offset: Int, limit: Int): List<ArticleEntity>
}
