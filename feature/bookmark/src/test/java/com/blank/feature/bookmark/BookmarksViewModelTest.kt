package com.blank.feature.bookmark

import app.cash.turbine.test
import com.blank.core.base.UiState
import com.blank.core.model.NewsItem
import com.blank.domain.model.ArticleModel
import com.blank.domain.usecase.GetBookmarksUseCase
import com.blank.domain.usecase.ToggleBookmarkUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @MockK
    private lateinit var getBookmarksUseCase: GetBookmarksUseCase

    @MockK
    private lateinit var toggleBookmarkUseCase: ToggleBookmarkUseCase

    private lateinit var viewModel: BookmarksViewModel

    companion object {
        private val testArticle1 = ArticleModel(
            sourceId = "techcrunch",
            sourceName = "TechCrunch",
            author = "John Doe",
            title = "Bookmarked Article 1",
            description = "Description 1",
            url = "https://example.com/bookmark1",
            urlToImage = "https://example.com/image1.jpg",
            publishedAt = "2024-01-15T10:00:00Z",
            content = "Content 1",
            isBookmarked = true
        )

        private val testArticle2 = ArticleModel(
            sourceId = "verge",
            sourceName = "The Verge",
            author = "Jane Smith",
            title = "Bookmarked Article 2",
            description = "Description 2",
            url = "https://example.com/bookmark2",
            urlToImage = "https://example.com/image2.jpg",
            publishedAt = "2024-01-14T15:30:00Z",
            content = "Content 2",
            isBookmarked = true
        )

        private val testArticle3 = ArticleModel(
            sourceId = "wired",
            sourceName = "Wired",
            author = "Bob Johnson",
            title = "Bookmarked Article 3",
            description = "Description 3",
            url = "https://example.com/bookmark3",
            urlToImage = "https://example.com/image3.jpg",
            publishedAt = "2024-01-13T08:00:00Z",
            content = "Content 3",
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
    fun `bookmarks should emit Loading as initial state`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(emptyList())

        // When
        createViewModel()

        // Then - initial state should be Loading
        assertTrue(viewModel.bookmarks.value is UiState.Loading)
    }

    @Test
    fun `bookmarks should emit Empty when no bookmarks exist`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(emptyList())

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // Get Empty state
            val state = awaitItem()
            assertTrue(state is UiState.Empty)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `bookmarks should emit Success with bookmarked articles`() = testScope.runTest {
        // Given
        val bookmarkedArticles = listOf(testArticle1, testArticle2)
        every { getBookmarksUseCase() } returns flowOf(bookmarkedArticles)

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // Get Success state
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            if (state is UiState.Success) {
                assertEquals(2, state.data.size)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `bookmarks should map articles to NewsItem correctly`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(listOf(testArticle1))

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // Get Success state
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            if (state is UiState.Success) {
                val newsItem = state.data.first()
                assertEquals(testArticle1.title, newsItem.title)
                assertEquals(testArticle1.sourceName, newsItem.source)
                assertTrue(newsItem.isBookmarked)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `toggleBookmark should call use case with correct article`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(listOf(testArticle1))
        coEvery { toggleBookmarkUseCase(any()) } returns com.blank.domain.base.Resource.Success(Unit)

        createViewModel()
        advanceUntilIdle()

        val newsItem = NewsItem(
            id = testArticle1.url,
            title = testArticle1.title,
            description = testArticle1.description,
            content = testArticle1.content,
            source = testArticle1.sourceName,
            timeAgo = "2h ago",
            category = "",
            urlToImage = testArticle1.urlToImage,
            url = testArticle1.url,
            author = testArticle1.author,
            isBookmarked = true
        )

        // When
        viewModel.toggleBookmark(newsItem)
        advanceUntilIdle()

        // Then
        coVerify { toggleBookmarkUseCase(any()) }
    }

    // ==================== NEGATIVE CASES ====================

    @Test
    fun `bookmarks should keep Loading state when use case throws exception`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flow { throw RuntimeException("Error") }

        // When
        createViewModel()

        // Then - should remain in Loading state when source throws
        viewModel.bookmarks.test {
            assertTrue(awaitItem() is UiState.Loading)
            // No more items because the source flow failed
            cancelAndConsumeRemainingEvents()
        }
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `bookmarks should handle single bookmark`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(listOf(testArticle1))

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // Get Success state
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            if (state is UiState.Success) {
                assertEquals(1, state.data.size)
                assertEquals(testArticle1.title, state.data.first().title)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `bookmarks should handle large number of bookmarks`() = testScope.runTest {
        // Given
        val manyArticles = (1..100).map { index ->
            testArticle1.copy(
                url = "https://example.com/article$index",
                title = "Article $index"
            )
        }
        every { getBookmarksUseCase() } returns flowOf(manyArticles)

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // Get Success state
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            if (state is UiState.Success) {
                assertEquals(100, state.data.size)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `bookmarks should update when bookmarks change`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(
            listOf(testArticle1),
            listOf(testArticle1, testArticle2),
            listOf(testArticle2)
        )

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // First emission
            val first = awaitItem()
            assertTrue(first is UiState.Success)
            if (first is UiState.Success) assertEquals(1, first.data.size)

            // Second emission
            val second = awaitItem()
            assertTrue(second is UiState.Success)
            if (second is UiState.Success) assertEquals(2, second.data.size)

            // Third emission
            val third = awaitItem()
            assertTrue(third is UiState.Success)
            if (third is UiState.Success) {
                assertEquals(1, third.data.size)
                assertEquals(testArticle2.title, third.data.first().title)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `toggleBookmark should handle article with empty fields`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(emptyList())
        coEvery { toggleBookmarkUseCase(any()) } returns com.blank.domain.base.Resource.Success(Unit)

        createViewModel()
        advanceUntilIdle()

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
            isBookmarked = true
        )

        // When/Then - should not crash
        viewModel.toggleBookmark(emptyArticle)
        advanceUntilIdle()
    }

    @Test
    fun `toggleBookmark should handle very long content`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(emptyList())
        coEvery { toggleBookmarkUseCase(any()) } returns com.blank.domain.base.Resource.Success(Unit)

        createViewModel()
        advanceUntilIdle()

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
            isBookmarked = true
        )

        // When
        viewModel.toggleBookmark(longContentArticle)
        advanceUntilIdle()

        // Then
        coVerify { toggleBookmarkUseCase(any()) }
    }

    @Test
    fun `bookmarks should handle unicode characters in articles`() = testScope.runTest {
        // Given
        val unicodeArticle = testArticle1.copy(
            title = "日本語タイトル 🎉",
            description = "Descrição em português",
            content = "Content with émojis 🚀 and ñ characters",
            author = "作者名"
        )
        every { getBookmarksUseCase() } returns flowOf(listOf(unicodeArticle))

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // Get Success state
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            if (state is UiState.Success) {
                assertEquals("日本語タイトル 🎉", state.data.first().title)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `bookmarks should handle special characters in URLs`() = testScope.runTest {
        // Given
        val specialUrlArticle = testArticle1.copy(
            url = "https://example.com/article?id=123&test=value#section",
            title = "Special URL Article"
        )
        every { getBookmarksUseCase() } returns flowOf(listOf(specialUrlArticle))

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // Get Success state
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            if (state is UiState.Success) {
                assertEquals("Special URL Article", state.data.first().title)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `bookmarks should set isBookmarked to true for all items`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(listOf(testArticle1, testArticle2, testArticle3))

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // Get Success state
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            if (state is UiState.Success) {
                assertTrue(state.data.all { it.isBookmarked })
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `toggleBookmark should handle use case error`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(listOf(testArticle1))
        coEvery { toggleBookmarkUseCase(any()) } returns com.blank.domain.base.Resource.Error("Failed")

        createViewModel()
        advanceUntilIdle()

        val newsItem = NewsItem(
            id = testArticle1.url,
            title = testArticle1.title,
            description = testArticle1.description,
            content = testArticle1.content,
            source = testArticle1.sourceName,
            timeAgo = "2h ago",
            category = "",
            urlToImage = testArticle1.urlToImage,
            url = testArticle1.url,
            author = testArticle1.author,
            isBookmarked = true
        )

        // When/Then - should not crash on error
        viewModel.toggleBookmark(newsItem)
        advanceUntilIdle()

        coVerify { toggleBookmarkUseCase(any()) }
    }

    @Test
    fun `bookmarks should handle articles with null optional fields`() = testScope.runTest {
        // Given
        val articleWithNulls = testArticle1.copy(
            author = "",
            description = "",
            content = "",
            urlToImage = ""
        )
        every { getBookmarksUseCase() } returns flowOf(listOf(articleWithNulls))

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // Get Success state
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            if (state is UiState.Success) {
                assertEquals("", state.data.first().author)
                assertEquals("", state.data.first().urlToImage)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `toggleBookmark should handle rapid toggleBookmark calls`() = testScope.runTest {
        // Given
        every { getBookmarksUseCase() } returns flowOf(listOf(testArticle1))
        coEvery { toggleBookmarkUseCase(any()) } returns com.blank.domain.base.Resource.Success(Unit)

        createViewModel()
        advanceUntilIdle()

        val newsItem = NewsItem(
            id = testArticle1.url,
            title = testArticle1.title,
            description = testArticle1.description,
            content = testArticle1.content,
            source = testArticle1.sourceName,
            timeAgo = "2h ago",
            category = "",
            urlToImage = testArticle1.urlToImage,
            url = testArticle1.url,
            author = testArticle1.author,
            isBookmarked = true
        )

        // When - rapid toggles
        repeat(10) {
            viewModel.toggleBookmark(newsItem)
        }
        advanceUntilIdle()

        // Then
        coVerify(exactly = 10) { toggleBookmarkUseCase(any()) }
    }

    @Test
    fun `bookmarks should handle articles with very long URLs`() = testScope.runTest {
        // Given
        val longUrl = "https://example.com/" + "a".repeat(1000)
        val longUrlArticle = testArticle1.copy(
            url = longUrl,
            title = "Long URL Article"
        )
        every { getBookmarksUseCase() } returns flowOf(listOf(longUrlArticle))

        // When
        createViewModel()

        // Then
        viewModel.bookmarks.test {
            // Skip Loading state
            awaitItem()
            // Get Success state
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            if (state is UiState.Success) {
                assertEquals("Long URL Article", state.data.first().title)
                assertEquals(longUrl, state.data.first().url)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun createViewModel() {
        viewModel = BookmarksViewModel(
            getBookmarksUseCase = getBookmarksUseCase,
            toggleBookmarkUseCase = toggleBookmarkUseCase
        )
    }
}
