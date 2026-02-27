package com.blank.domain.usecase

import com.blank.domain.model.ArticleModel
import com.blank.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
) {

    operator fun invoke(): Flow<List<ArticleModel>> {
        return bookmarkRepository.getAllBookmarks()
    }
}
