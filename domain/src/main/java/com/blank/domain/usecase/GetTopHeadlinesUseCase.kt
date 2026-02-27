package com.blank.domain.usecase

import androidx.paging.PagingData
import com.blank.domain.model.ArticleModel
import com.blank.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTopHeadlinesUseCase @Inject constructor(
    private val newsRepository: NewsRepository,
) {

    operator fun invoke(
        country: String? = "us",
        category: String? = null,
        query: String? = null,
    ): Flow<PagingData<ArticleModel>> {
        return newsRepository.getTopHeadlines(
            country = country,
            category = category,
            query = query,
        )
    }
}
