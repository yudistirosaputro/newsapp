package com.blank.domain.base

interface BaseMapper<in FROM, out TO> {
    fun map(from: FROM): TO
}

interface BaseListMapper<in FROM, out TO> : BaseMapper<List<FROM>, List<TO>> {
    override fun map(from: List<FROM>): List<TO> {
        return from.map { mapItem(it) }
    }

    fun mapItem(from: FROM): TO
}
