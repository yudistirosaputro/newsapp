package com.blank.domain.usecase

import com.blank.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarkedUrlsUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
) {

    operator fun invoke(): Flow<Set<String>> {
        return bookmarkRepository.getBookmarkedUrls()
    }
}
