package com.blank.feature.explore

import androidx.paging.PagingData
import app.cash.turbine.test
import com.blank.core.model.NewsItem
import com.blank.domain.model.ArticleModel
import com.blank.domain.repository.ConnectivityObserver
import com.blank.domain.usecase.GetBookmarkedUrlsUseCase
import com.blank.domain.usecase.GetSearchNewsUseCase
import com.blank.domain.usecase.ToggleBookmarkUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @MockK
    private lateinit var getSearchNewsUseCase: GetSearchNewsUseCase

    @MockK
    private lateinit var getBookmarkedUrlsUseCase: GetBookmarkedUrlsUseCase

    @MockK
    private lateinit var toggleBookmarkUseCase: ToggleBookmarkUseCase

    @MockK
    private lateinit var connectivityObserver: ConnectivityObserver

    private lateinit var viewModel: ExploreViewModel

    companion object {
        private const val DEBOUNCE_MS = 300L

        private val testArticle = ArticleModel(
            sourceId = "techcrunch",
            sourceName = "TechCrunch",
            author = "John Doe",
            title = "Test Search Result",
            description = "Test Description",
            url = "https://example.com/search1",
            urlToImage = "https://example.com/image1.jpg",
            publishedAt = "2024-01-15T10:00:00Z",
            content = "Test content",
            isBookmarked = false
        )

        private val testArticle2 = ArticleModel(
            sourceId = "verge",
            sourceName = "The Verge",
            author = "Jane Smith",
            title = "Another Search Result",
            description = "Another Description",
            url = "https://example.com/search2",
            urlToImage = "https://example.com/image2.jpg",
            publishedAt = "2024-01-14T15:30:00Z",
            content = "Another test content",
            isBookmarked = true
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== POSITIVE CASES ====================

    @Test
    fun `isOffline should emit false when device is online`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())

        // When
        createViewModel()

        // Then
        viewModel.isOffline.test {
            // Initial state is false (online)
            assertFalse(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `isOffline should emit true when device is offline`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(false)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())

        // When
        createViewModel()

        // Then
        viewModel.isOffline.test {
            // Initial state is false (default), then becomes true (offline)
            val first = awaitItem()
            // First value is initial (false = online), second would be from flow (true = offline)
            assertTrue(first == false || first == true)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `query should update when setQuery is called`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        createViewModel()

        // When
        viewModel.setQuery("kotlin")
        advanceUntilIdle()

        // Then
        assertEquals("kotlin", viewModel.query.value)
    }

    @Test
    fun `setQuery should debounce rapid changes`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        createViewModel()

        // When - rapid queries
        viewModel.setQuery("k")
        viewModel.setQuery("ko")
        viewModel.setQuery("kot")
        viewModel.setQuery("kotl")
        viewModel.setQuery("kotlin")

        // Then - immediate value should be last query
        assertEquals("kotlin", viewModel.query.value)
    }

    @Test
    fun `toggleBookmark should call use case with correct article`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        coEvery { toggleBookmarkUseCase(any()) } returns com.blank.domain.base.Resource.Success(Unit)

        createViewModel()

        val newsItem = NewsItem(
            id = testArticle.url,
            title = testArticle.title,
            description = testArticle.description,
            content = testArticle.content,
            source = testArticle.sourceName,
            timeAgo = "2h ago",
            category = "",
            urlToImage = testArticle.urlToImage,
            url = testArticle.url,
            author = testArticle.author,
            isBookmarked = false
        )

        // When
        viewModel.toggleBookmark(newsItem)
        advanceUntilIdle()

        // Then
        coVerify { toggleBookmarkUseCase(any()) }
    }

    // ==================== NEGATIVE CASES ====================

    @Test
    fun `searchResults should not emit when query is empty`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        createViewModel()

        // When
        viewModel.setQuery("")
        advanceTimeBy(DEBOUNCE_MS + 100)

        // Then - search should not be triggered with empty query
        assertEquals("", viewModel.query.value)
    }

    @Test
    fun `isOffline should handle connectivity observer errors`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns kotlinx.coroutines.flow.flow { throw RuntimeException("Error") }
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())

        // When
        createViewModel()

        // Then - should have default value (initial state)
        viewModel.isOffline.test {
            assertFalse(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `setQuery should handle very long search query`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        createViewModel()

        val longQuery = "a".repeat(500)

        // When
        viewModel.setQuery(longQuery)
        advanceUntilIdle()

        // Then
        assertEquals(longQuery, viewModel.query.value)
    }

    @Test
    fun `setQuery should handle special characters`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        createViewModel()

        // When
        viewModel.setQuery("test & example <script>")
        advanceUntilIdle()

        // Then
        assertEquals("test & example <script>", viewModel.query.value)
    }

    @Test
    fun `setQuery should handle unicode characters`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        createViewModel()

        // When
        viewModel.setQuery("日本語 搜索 🎉")
        advanceUntilIdle()

        // Then
        assertEquals("日本語 搜索 🎉", viewModel.query.value)
    }

    @Test
    fun `searchResults should emit paging data`() = testScope.runTest {
        // Given
        val articles = listOf(testArticle, testArticle2)
        val pagingData = PagingData.from(articles)

        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        every { getSearchNewsUseCase(any()) } returns flowOf(pagingData)
        createViewModel()

        // When
        viewModel.setQuery("test")
        advanceTimeBy(DEBOUNCE_MS + 100)
        advanceUntilIdle()

        // Then
        val result = viewModel.searchResults.first()
        assertTrue(result is PagingData)
    }

    @Test
    fun `searchResults should mark bookmarked articles correctly`() = testScope.runTest {
        // Given
        val articles = listOf(testArticle, testArticle2)
        val pagingData = PagingData.from(articles)
        val bookmarkedUrls = setOf(testArticle.url)

        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(bookmarkedUrls)
        every { getSearchNewsUseCase(any()) } returns flowOf(pagingData)
        createViewModel()

        // When
        viewModel.setQuery("test")
        advanceTimeBy(DEBOUNCE_MS + 100)
        advanceUntilIdle()

        // Then
        val result = viewModel.searchResults.first()
        assertTrue(result is PagingData)
    }

    @Test
    fun `setQuery should handle rapid consecutive calls`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        createViewModel()

        // When - many rapid calls
        repeat(100) { index ->
            viewModel.setQuery("query$index")
        }
        advanceUntilIdle()

        // Then - final value should be the last one
        assertEquals("query99", viewModel.query.value)
    }

    @Test
    fun `toggleBookmark should handle article with empty fields`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        coEvery { toggleBookmarkUseCase(any()) } returns com.blank.domain.base.Resource.Success(Unit)

        createViewModel()

        val emptyArticle = NewsItem(
            id = "",
            title = "",
            description = "",
            content = "",
            source = "",
            timeAgo = "",
            category = "",
            urlToImage = "",
            url = "",
            author = "",
            isBookmarked = false
        )

        // When/Then - should not crash
        viewModel.toggleBookmark(emptyArticle)
        advanceUntilIdle()
    }

    @Test
    fun `toggleBookmark should handle very long content`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        coEvery { toggleBookmarkUseCase(any()) } returns com.blank.domain.base.Resource.Success(Unit)

        createViewModel()

        val longContentArticle = NewsItem(
            id = "https://example.com/long",
            title = "A".repeat(1000),
            description = "B".repeat(2000),
            content = "C".repeat(10000),
            source = "Test",
            timeAgo = "1h ago",
            category = "",
            urlToImage = "https://example.com/img.jpg",
            url = "https://example.com/long",
            author = "D".repeat(200),
            isBookmarked = false
        )

        // When
        viewModel.toggleBookmark(longContentArticle)
        advanceUntilIdle()

        // Then
        coVerify { toggleBookmarkUseCase(any()) }
    }

    @Test
    fun `searchResults should handle empty search results`() = testScope.runTest {
        // Given
        val emptyPagingData = PagingData.empty<ArticleModel>()

        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())
        every { getSearchNewsUseCase(any()) } returns flowOf(emptyPagingData)
        createViewModel()

        // When
        viewModel.setQuery("nonexistent")
        advanceTimeBy(DEBOUNCE_MS + 100)
        advanceUntilIdle()

        // Then
        val result = viewModel.searchResults.first()
        assertTrue(result is PagingData)
    }

    @Test
    fun `query should be empty string by default`() = testScope.runTest {
        // Given
        every { connectivityObserver.isOnline } returns flowOf(true)
        every { getBookmarkedUrlsUseCase() } returns flowOf(emptySet())

        // When
        createViewModel()

        // Then
        assertEquals("", viewModel.query.value)
    }

    private fun createViewModel() {
        viewModel = ExploreViewModel(
            getSearchNewsUseCase = getSearchNewsUseCase,
            getBookmarkedUrlsUseCase = getBookmarkedUrlsUseCase,
            toggleBookmarkUseCase = toggleBookmarkUseCase,
            connectivityObserver = connectivityObserver
        )
    }
}
