# Android NewsApp Skill (Vibe Edition)
## Kotlin + XML + Clean Architecture + MVVM

> Use this skill whenever working on the NewsApp project.
> Trigger: any Android/Kotlin task in this repository.
> **Vibe Check:** Generate these templates quickly, keep the momentum high, and ensure every snippet is clean and secure by default.

---

## Project Quick Reference

**Tech:** Kotlin 2.3.10, AGP 9.0.1, Hilt 2.59.2, Retrofit 2.11.0, Room 2.8.4, Paging 3.4.1, Coil 2.7.0
**Architecture:** Clean Architecture — `:domain` (pure Kotlin) ← `:data` → `:feature:*` (MVVM)
**UI:** XML Views + ViewBinding (no Compose)
**DI:** Hilt (Dagger-backed)

---

## File Creation Templates

### New UseCase (`:domain`)
```kotlin
// domain/src/main/java/com/blank/domain/usecase/{Verb}{Noun}UseCase.kt
package com.blank.domain.usecase

import javax.inject.Inject

class {Verb}{Noun}UseCase @Inject constructor(
    private val repository: {Noun}Repository,
) {
    operator fun invoke(/* params */): /* Return type */ {
        return repository.{method}(/* params */)
    }
}
```

### New Repository Interface (`:domain`)
```kotlin
// domain/src/main/java/com/blank/domain/repository/{Noun}Repository.kt
package com.blank.domain.repository

interface {Noun}Repository {
    // ONLY pure Kotlin types — no android.*, no Room entities, no DTOs
}
```

### New Room Entity (`:data`)
```kotlin
// data/src/main/kotlin/com/blank/data/local/entity/{Noun}Entity.kt
package com.blank.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "{table_name}")
data class {Noun}Entity(
    @PrimaryKey val url: String,
    // fields...
    val cachedAt: Long = System.currentTimeMillis(),
)
```

### New DAO (`:data`)
```kotlin
// data/src/main/kotlin/com/blank/data/local/dao/{Noun}Dao.kt
package com.blank.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface {Noun}Dao {
    @Query("SELECT * FROM {table_name}")
    fun getAll(): Flow<List<{Noun}Entity>>

    @Upsert
    suspend fun upsertAll(items: List<{Noun}Entity>)

    @Query("DELETE FROM {table_name}")
    suspend fun deleteAll()
}
```

### New Repository Implementation (`:data`)
```kotlin
// data/src/main/kotlin/com/blank/data/repository/{Noun}RepositoryImpl.kt
package com.blank.data.repository

import com.blank.domain.base.Resource
import com.blank.domain.repository.{Noun}Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class {Noun}RepositoryImpl @Inject constructor(
    private val dao: {Noun}Dao,
    private val mapper: {Noun}EntityMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : {Noun}Repository {

    override fun getAll() = dao.getAll()
        .map { entities -> mapper.map(entities) }

    override suspend fun refresh(): Resource<Unit> = withContext(ioDispatcher) {
        try {
            // network call → Room upsert
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Exception(e)
        }
    }
}
```

### New PagingSource (`:data`)
```kotlin
// data/src/main/kotlin/com/blank/data/remote/paging/{Noun}PagingSource.kt
package com.blank.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.blank.data.remote.helper.NetworkResponse
import com.blank.domain.model.ArticleModel
import javax.inject.Inject

class {Noun}PagingSource(
    private val apiService: NewsApiService,
    private val mapper: ArticleMapper,
    // params...
) : PagingSource<Int, ArticleModel>() {

    companion object {
        const val PAGE_SIZE = 5          // FIXED — assessment requires max 5
        private const val STARTING_PAGE = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleModel> {
        val page = params.key ?: STARTING_PAGE
        return when (val response = apiService.{endpoint}(pageSize = params.loadSize, page = page)) {
            is NetworkResponse.Success -> {
                val articles = mapper.map(response.data.articles)
                val totalResults = response.data.totalResults
                LoadResult.Page(
                    data = articles,
                    prevKey = if (page == STARTING_PAGE) null else page - 1,
                    nextKey = if (articles.isEmpty() || page * params.loadSize >= totalResults) null else page + 1,
                )
            }
            is NetworkResponse.ErrorApi -> LoadResult.Error(Exception("API Error ${response.code}: ${response.error.message}"))
            is NetworkResponse.ErrorNetwork -> LoadResult.Error(response.error)
            is NetworkResponse.ErrorUnknown -> LoadResult.Error(response.error ?: Exception("Unknown error"))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ArticleModel>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
}
```

### New ViewModel (`:feature:*`)
```kotlin
// feature/{name}/src/main/java/com/blank/feature/{name}/{Feature}ViewModel.kt
package com.blank.feature.{name}

import androidx.lifecycle.viewModelScope
import com.blank.core.base.BaseViewModel
import com.blank.core.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class {Feature}ViewModel @Inject constructor(
    private val {verb}{Noun}UseCase: {Verb}{Noun}UseCase,
) : BaseViewModel() {

    private val _state = MutableStateFlow<UiState<{Model}>>(UiState.Loading)
    val state: StateFlow<UiState<{Model}>> = _state.asStateFlow()

    init { load() }

    fun load() {
        launchWithLoading(
            block = { {verb}{Noun}UseCase() },
            onSuccess = { resource ->
                when (resource) {
                    is Resource.Success -> _state.value = UiState.Success(resource.data)
                    is Resource.Error -> _state.value = UiState.Error(resource.message)
                    is Resource.Exception -> _state.value = UiState.Error(resource.throwable.message ?: "Error")
                }
            },
        )
    }
}
```

### New Fragment (`:feature:*`)
```kotlin
// feature/{name}/src/main/java/com/blank/feature/{name}/{Feature}Fragment.kt
package com.blank.feature.{name}

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.blank.core.base.BaseFragment
import com.blank.core.base.UiState
import com.blank.core.extensions.collectWithLifecycle
import com.blank.core.extensions.gone
import com.blank.core.extensions.visible
import com.blank.feature.{name}.databinding.Fragment{Feature}Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class {Feature}Fragment : BaseFragment<Fragment{Feature}Binding>(Fragment{Feature}Binding::inflate) {

    private val viewModel: {Feature}ViewModel by viewModels()

    override fun onViewReady(savedInstanceState: Bundle?) {
        // setup views, adapters, click listeners
    }

    override fun observeState() {
        collectWithLifecycle(viewModel.state) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visible()
                    binding.rvContent.gone()
                }
                is UiState.Success -> {
                    binding.progressBar.gone()
                    binding.rvContent.visible()
                    // adapter.submitList(state.data) or adapter.submitData(state.data)
                }
                is UiState.Error -> {
                    binding.progressBar.gone()
                    showToast(state.message)
                }
                is UiState.Empty -> {
                    binding.progressBar.gone()
                    binding.emptyState.visible()
                }
            }
        }
    }
}
```

### New PagingDataAdapter (`:feature:*`)
```kotlin
// feature/{name}/src/main/java/com/blank/feature/{name}/adapter/{Noun}Adapter.kt
package com.blank.feature.{name}.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blank.feature.{name}.databinding.Item{Noun}Binding
import com.blank.feature.{name}.model.NewsItem

class {Noun}Adapter(
    private val onItemClick: (NewsItem) -> Unit,
    private val onBookmarkClick: (NewsItem) -> Unit,
) : PagingDataAdapter<NewsItem, {Noun}Adapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: Item{Noun}Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NewsItem) = binding.apply {
            tvTitle.text = item.title
            tvSource.text = item.source
            tvTime.text = item.timeAgo
            ivThumbnail.load(item.urlToImage) {
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_placeholder)
                crossfade(true)
            }
            val bookmarkIcon = if (item.isBookmarked) R.drawable.ic_bookmark_filled
                               else R.drawable.ic_bookmark_outline
            ibBookmark.setImageResource(bookmarkIcon)
            root.setOnClickListener { onItemClick(item) }
            ibBookmark.setOnClickListener { onBookmarkClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        Item{Noun}Binding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NewsItem>() {
            override fun areItemsTheSame(o: NewsItem, n: NewsItem) = o.url == n.url
            override fun areContentsTheSame(o: NewsItem, n: NewsItem) = o == n
        }
    }
}
```

---

## Hilt Module Templates

### DatabaseModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "news_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideArticleDao(db: AppDatabase): ArticleDao = db.articleDao()
}
```

### CoroutineDispatcher Qualifier + Module
```kotlin
// core or data module
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides @IoDispatcher fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    @Provides @MainDispatcher fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
```

---

## Test Templates

### ViewModel Test with MainDispatcherRule
```kotlin
@RunWith(JUnit4::class)
class {Feature}ViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mock{UseCase}: {UseCase} = mockk()
    private lateinit var viewModel: {Feature}ViewModel

    @Before
    fun setup() {
        every { mock{UseCase}.invoke(any()) } returns flowOf(/* test data */)
        viewModel = {Feature}ViewModel(mock{UseCase})
    }

    @Test
    fun `given success response, when load called, then state is Success`() = runTest {
        // Given
        val expected = listOf(/* test data */)
        every { mock{UseCase}.invoke() } returns flowOf(Resource.Success(expected))

        // When
        viewModel.load()

        // Then
        viewModel.state.test {
            assertThat(awaitItem()).isInstanceOf(UiState.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### PagingSource Test
```kotlin
@RunWith(JUnit4::class)
class {Noun}PagingSourceTest {

    private val mockApiService: NewsApiService = mockk()
    private val mockMapper: ArticleMapper = mockk()
    private lateinit var pagingSource: {Noun}PagingSource

    @Before
    fun setup() {
        pagingSource = {Noun}PagingSource(mockApiService, mockMapper, /* params */)
    }

    @Test
    fun `given successful response, when load called, then returns Page with data`() = runTest {
        // Given
        val mockArticles = listOf(/* test ArticleDto */)
        val mockResponse = NetworkResponse.Success(BaseResponse(status = "ok", totalResults = 10, articles = mockArticles))
        coEvery { mockApiService.{endpoint}(any(), any()) } returns mockResponse
        every { mockMapper.map(any()) } returns listOf(/* test ArticleModel */)

        // When
        val params = PagingSource.LoadParams.Refresh(null, 5, false)
        val result = pagingSource.load(params)

        // Then
        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).hasSize(/* expected size */)
    }
}
```

---

## Common Pitfalls & Solutions

| Pitfall | Symptom | Solution |
|---------|---------|----------|
| `!!` on `arguments?.getParcelable()` | NullPointerException on config change | `?: throw IllegalArgumentException("...")` |
| Collecting Flow without repeatOnLifecycle | Memory leak / double collection | Always use `collectWithLifecycle()` from BaseFragment |
| Exposing MutableStateFlow | UI can push state | `val state: StateFlow<T> = _state.asStateFlow()` |
| Hardcoded Dispatchers.IO in Room | TestCoroutineDispatcher ignored in tests | Inject `@IoDispatcher` dispatcher |
| `cachedIn` missing on PagingData | Reloads on rotation | `.cachedIn(viewModelScope)` in ViewModel |
| PagingData bookmark not updating | Items show stale bookmark state | `combine(pagingFlow, bookmarkIds)` pattern |
| Room returns same cached value | `@Query` not observing changes | Ensure DAO returns `Flow` not `suspend` |
| SafeArgs not generated | Build error on navigate | Register fragment in nav_graph.xml first |

---

## Build Verification Commands

```bash
# Compile check (fast)
./gradlew :data:compileDebugKotlin
./gradlew :domain:compileDebugKotlin
./gradlew :feature:home:compileDebugKotlin

# Full build
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run lint
./gradlew lintDebug

# Clean build
./gradlew clean assembleDebug
```
