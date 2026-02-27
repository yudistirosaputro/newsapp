package com.blank.data.mapper

import com.blank.data.remote.dto.ArticleDto
import com.blank.domain.base.BaseListMapper
import com.blank.domain.model.ArticleModel

class ArticleMapper : BaseListMapper<ArticleDto, ArticleModel> {

    override fun mapItem(from: ArticleDto): ArticleModel {
        return ArticleModel(
            sourceId = from.source?.id.orEmpty(),
            sourceName = from.source?.name.orEmpty(),
            author = from.author.orEmpty(),
            title = from.title.orEmpty(),
            description = from.description.orEmpty(),
            url = from.url.orEmpty(),
            urlToImage = from.urlToImage.orEmpty(),
            publishedAt = from.publishedAt.orEmpty(),
            content = from.content.orEmpty(),
        )
    }
}