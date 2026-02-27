package com.blank.domain.usecase

import com.blank.domain.base.Resource
import com.blank.domain.model.ArticleModel
import com.blank.domain.repository.BookmarkRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
) {

    suspend operator fun invoke(article: ArticleModel): Resource<Unit> {
        return bookmarkRepository.toggleBookmark(article)
    }
}
