package com.blank.domain.usecase

import androidx.paging.PagingData
import com.blank.domain.model.ArticleModel
import com.blank.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSearchNewsUseCase @Inject constructor(
    private val searchRepository: SearchRepository,
) {

    operator fun invoke(query: String): Flow<PagingData<ArticleModel>> {
        return searchRepository.searchNews(query)
    }
}
