package com.blank.data.di

import com.blank.data.connectivity.AndroidConnectivityObserver
import com.blank.data.repository.BookmarkRepositoryImpl
import com.blank.data.repository.NewsRepositoryImpl
import com.blank.data.repository.SearchRepositoryImpl
import com.blank.domain.repository.BookmarkRepository
import com.blank.domain.repository.ConnectivityObserver
import com.blank.domain.repository.NewsRepository
import com.blank.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        newsRepositoryImpl: NewsRepositoryImpl,
    ): NewsRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        bookmarkRepositoryImpl: BookmarkRepositoryImpl,
    ): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl,
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(
        androidConnectivityObserver: AndroidConnectivityObserver,
    ): ConnectivityObserver
}
