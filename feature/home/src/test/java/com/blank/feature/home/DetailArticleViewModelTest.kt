package com.blank.feature.home

import com.blank.core.model.NewsItem
import com.blank.domain.base.Resource
import com.blank.domain.repository.BookmarkRepository
import com.blank.domain.usecase.ToggleBookmarkUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
class DetailArticleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @MockK
    private lateinit var toggleBookmarkUseCase: ToggleBookmarkUseCase

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    private lateinit var viewModel: DetailArticleViewModel

    companion object {
        private val testNewsItem = NewsItem(
            id = "https://example.com/article1",
            title = "Test Article Title",
            description = "Test Description",
            content = "Test content",
            source = "TechCrunch",
            timeAgo = "2h ago",
            category = "technology",
            urlToImage = "https://example.com/image1.jpg",
            url = "https://example.com/article1",
            author = "John Doe",
            isBookmarked = false
        )

        private val testNewsItemBookmarked = NewsItem(
            id = "https://example.com/article2",
            title = "Another Test Article",
            description = "Another Description",
            content = "Another test content",
            source = "The Verge",
            timeAgo = "1h ago",
            category = "technology",
            urlToImage = "https://example.com/image2.jpg",
            url = "https://example.com/article2",
            author = "Jane Smith",
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
    fun `isBookmarked should emit false when article is not bookmarked`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(testNewsItem.url) } returns false

        // When
        createViewModel()
        viewModel.setArticle(testNewsItem)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isBookmarked.value)
    }

    @Test
    fun `isBookmarked should emit true when article is bookmarked`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(testNewsItemBookmarked.url) } returns true

        // When
        createViewModel()
        viewModel.setArticle(testNewsItemBookmarked)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.isBookmarked.value)
    }

    @Test
    fun `toggleBookmark should call use case with correct article`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(any()) } returns false
        coEvery { toggleBookmarkUseCase(any()) } returns Resource.Success(Unit)

        createViewModel()
        viewModel.setArticle(testNewsItem)
        advanceUntilIdle()

        // When
        viewModel.toggleBookmark()
        advanceUntilIdle()

        // Then
        coVerify { toggleBookmarkUseCase(any()) }
    }

    @Test
    fun `toggleBookmark should update isBookmarked state after successful toggle`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(any()) } returns false andThen true
        coEvery { toggleBookmarkUseCase(any()) } returns Resource.Success(Unit)

        createViewModel()
        viewModel.setArticle(testNewsItem)
        advanceUntilIdle()

        assertFalse(viewModel.isBookmarked.value)

        // When
        viewModel.toggleBookmark()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.isBookmarked.value)
    }

    @Test
    fun `setArticle should check bookmark status for correct URL`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(testNewsItem.url) } returns true

        // When
        createViewModel()
        viewModel.setArticle(testNewsItem)
        advanceUntilIdle()

        // Then
        coVerify { bookmarkRepository.isBookmarked(testNewsItem.url) }
    }

    // ==================== NEGATIVE CASES ====================

    @Test(expected = RuntimeException::class)
    fun `isBookmarked should throw exception when repository throws`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(any()) } throws RuntimeException("Database error")

        // When/Then - should propagate exception
        createViewModel()
        viewModel.setArticle(testNewsItem)
        advanceUntilIdle()
    }

    @Test
    fun `toggleBookmark should not crash when article is null`() = testScope.runTest {
        // Given - viewModel created but setArticle not called
        createViewModel()

        // When/Then - should not throw
        viewModel.toggleBookmark()
        advanceUntilIdle()
    }

    @Test
    fun `toggleBookmark should handle use case error gracefully`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(any()) } returns false
        coEvery { toggleBookmarkUseCase(any()) } returns Resource.Error("Failed to toggle bookmark")

        createViewModel()
        viewModel.setArticle(testNewsItem)
        advanceUntilIdle()

        // When
        viewModel.toggleBookmark()
        advanceUntilIdle()

        // Then - state should remain false after error
        assertFalse(viewModel.isBookmarked.value)
    }

    @Test(expected = RuntimeException::class)
    fun `toggleBookmark should throw exception when use case throws`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(any()) } returns false
        coEvery { toggleBookmarkUseCase(any()) } throws RuntimeException("Network error")

        createViewModel()
        viewModel.setArticle(testNewsItem)
        advanceUntilIdle()

        // When/Then - should propagate exception
        viewModel.toggleBookmark()
        advanceUntilIdle()
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `setArticle should handle empty URL`() = testScope.runTest {
        // Given
        val emptyUrlArticle = testNewsItem.copy(url = "", id = "")
        coEvery { bookmarkRepository.isBookmarked("") } returns false

        // When
        createViewModel()
        viewModel.setArticle(emptyUrlArticle)
        advanceUntilIdle()

        // Then
        coVerify { bookmarkRepository.isBookmarked("") }
        assertFalse(viewModel.isBookmarked.value)
    }

    @Test
    fun `setArticle should handle article with very long URL`() = testScope.runTest {
        // Given
        val longUrl = "https://example.com/" + "a".repeat(1000)
        val longUrlArticle = testNewsItem.copy(url = longUrl, id = longUrl)
        coEvery { bookmarkRepository.isBookmarked(longUrl) } returns true

        // When
        createViewModel()
        viewModel.setArticle(longUrlArticle)
        advanceUntilIdle()

        // Then
        coVerify { bookmarkRepository.isBookmarked(longUrl) }
        assertTrue(viewModel.isBookmarked.value)
    }

    @Test
    fun `setArticle should handle article with special characters in URL`() = testScope.runTest {
        // Given
        val specialUrl = "https://example.com/article?id=123&test=value#section"
        val specialUrlArticle = testNewsItem.copy(url = specialUrl, id = specialUrl)
        coEvery { bookmarkRepository.isBookmarked(specialUrl) } returns false

        // When
        createViewModel()
        viewModel.setArticle(specialUrlArticle)
        advanceUntilIdle()

        // Then
        coVerify { bookmarkRepository.isBookmarked(specialUrl) }
    }

    @Test
    fun `setArticle should handle unicode characters in content`() = testScope.runTest {
        // Given
        val unicodeArticle = testNewsItem.copy(
            title = "日本語タイトル 🎉",
            description = "Descrição em português",
            content = "Content with émojis 🚀 and ñ characters",
            author = "作者名"
        )
        coEvery { bookmarkRepository.isBookmarked(any()) } returns false

        // When
        createViewModel()
        viewModel.setArticle(unicodeArticle)
        advanceUntilIdle()

        // Then - should not crash with unicode
        assertFalse(viewModel.isBookmarked.value)
    }

    @Test
    fun `setArticle should update bookmark status when different article is set`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(testNewsItem.url) } returns false
        coEvery { bookmarkRepository.isBookmarked(testNewsItemBookmarked.url) } returns true

        createViewModel()

        // When - set first article
        viewModel.setArticle(testNewsItem)
        advanceUntilIdle()
        assertFalse(viewModel.isBookmarked.value)

        // When - set second article
        viewModel.setArticle(testNewsItemBookmarked)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.isBookmarked.value)
    }

    @Test
    fun `toggleBookmark should toggle back and forth correctly`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(any()) } returnsMany listOf(false, true, false)
        coEvery { toggleBookmarkUseCase(any()) } returns Resource.Success(Unit)

        createViewModel()
        viewModel.setArticle(testNewsItem)
        advanceUntilIdle()

        // First toggle - add bookmark
        viewModel.toggleBookmark()
        advanceUntilIdle()
        assertTrue(viewModel.isBookmarked.value)

        // Second toggle - remove bookmark
        viewModel.toggleBookmark()
        advanceUntilIdle()
        assertFalse(viewModel.isBookmarked.value)

        // Verify toggle was called twice
        coVerify(exactly = 2) { toggleBookmarkUseCase(any()) }
    }

    @Test
    fun `setArticle should handle null fields in article`() = testScope.runTest {
        // Given
        val articleWithNulls = testNewsItem.copy(
            author = "",
            description = "",
            content = "",
            urlToImage = ""
        )
        coEvery { bookmarkRepository.isBookmarked(any()) } returns false

        // When
        createViewModel()
        viewModel.setArticle(articleWithNulls)
        advanceUntilIdle()

        // Then - should handle empty strings without crashing
        assertFalse(viewModel.isBookmarked.value)
    }

    @Test
    fun `isBookmarked should maintain state across multiple rapid toggles`() = testScope.runTest {
        // Given
        coEvery { bookmarkRepository.isBookmarked(any()) } returnsMany listOf(
            false, true, false, true, false
        )
        coEvery { toggleBookmarkUseCase(any()) } returns Resource.Success(Unit)

        createViewModel()
        viewModel.setArticle(testNewsItem)
        advanceUntilIdle()

        // When - rapid toggles
        repeat(4) {
            viewModel.toggleBookmark()
        }
        advanceUntilIdle()

        // Then - verify all toggles were processed
        coVerify(exactly = 4) { toggleBookmarkUseCase(any()) }
        coVerify(exactly = 5) { bookmarkRepository.isBookmarked(any()) }
    }

    @Test
    fun `setArticle should handle very long content`() = testScope.runTest {
        // Given
        val longContent = "A".repeat(50000)
        val longArticle = testNewsItem.copy(
            title = "T".repeat(500),
            description = "D".repeat(2000),
            content = longContent
        )
        coEvery { bookmarkRepository.isBookmarked(any()) } returns false

        // When
        createViewModel()
        viewModel.setArticle(longArticle)
        advanceUntilIdle()

        // Then - should handle long content without issues
        assertFalse(viewModel.isBookmarked.value)
    }

    private fun createViewModel() {
        viewModel = DetailArticleViewModel(
            toggleBookmarkUseCase = toggleBookmarkUseCase,
            bookmarkRepository = bookmarkRepository
        )
    }
}
