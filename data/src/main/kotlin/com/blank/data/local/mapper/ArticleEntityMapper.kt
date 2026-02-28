package com.blank.data.local.mapper

import com.blank.data.local.entity.ArticleEntity
import com.blank.domain.base.BaseListMapper
import com.blank.domain.model.ArticleModel

class ArticleEntityMapper : BaseListMapper<ArticleEntity, ArticleModel> {

    override fun mapItem(from: ArticleEntity): ArticleModel {
        return ArticleModel(
            sourceId = from.sourceId,
            sourceName = from.sourceName,
            author = from.author,
            title = from.title,
            description = from.description,
            url = from.url,
            urlToImage = from.urlToImage,
            publishedAt = from.publishedAt,
            content = from.content,
            isBookmarked = from.isBookmarked,
        )
    }
}

fun ArticleModel.toEntity(
    isTopHeadline: Boolean = false,
    isBookmarked: Boolean = false,
): ArticleEntity {
    return ArticleEntity(
        url = url,
        sourceId = sourceId,
        sourceName = sourceName,
        author = author,
        title = title,
        description = description,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content,
        isTopHeadline = isTopHeadline,
        isBookmarked = isBookmarked,
    )
}
