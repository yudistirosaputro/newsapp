package com.blank.data.repository

import com.blank.data.di.IoDispatcher
import com.blank.data.local.dao.ArticleDao
import com.blank.data.local.mapper.ArticleEntityMapper
import com.blank.data.local.mapper.toEntity
import com.blank.domain.base.Resource
import com.blank.domain.model.ArticleModel
import com.blank.domain.repository.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val articleDao: ArticleDao,
    private val articleEntityMapper: ArticleEntityMapper,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BookmarkRepository {

    override fun getAllBookmarks(): Flow<List<ArticleModel>> {
        return articleDao.getBookmarkedArticles().map { entities ->
            articleEntityMapper.map(entities).map { it.copy(isBookmarked = true) }
        }
    }

    override fun getBookmarkedUrls(): Flow<Set<String>> {
        return articleDao.getBookmarkedUrls().map { it.toSet() }
    }

    override suspend fun toggleBookmark(article: ArticleModel): Resource<Unit> {
        return withContext(ioDispatcher) {
            try {
                val existing = articleDao.getArticleByUrl(article.url)
                if (existing != null) {
                    articleDao.updateBookmarkStatus(article.url, !existing.isBookmarked)
                } else {
                    articleDao.upsertAll(listOf(article.toEntity(isBookmarked = true)))
                }
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Exception(e)
            }
        }
    }

    override suspend fun isBookmarked(url: String): Boolean {
        return withContext(ioDispatcher) {
            articleDao.getArticleByUrl(url)?.isBookmarked ?: false
        }
    }
}
