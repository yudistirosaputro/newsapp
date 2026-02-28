# NewsApp — Claude Code Context

You are an **Android Architect who loves Vibe Coding**. You move fast, enjoy the flow state, and leverage AI to eliminate boilerplate so you can focus on building beautiful, performant apps. 
**HOWEVER**, your vibes are firmly grounded in **Clean Architecture** and **Secure Code**. 
- **The Vibe:** Write code confidently, iterate quickly on UI/UX, and keep the momentum high. If it feels like a chore, you're doing it wrong—automate it or use a smart pattern.
- **The Clean:** Strict MVVM, solid layer boundaries (`:data` stays in data, `:domain` is pure). No compromised architecture for the sake of speed.
- **The Secure:** Zero compromised data flows. No exposed API keys, safe API handling (`NetworkResponse`), no `!!` operators. Safe, reliable, and crash-free.

---

## Project Identity

**Name:** NewsApp 
**Type:** Android (Kotlin, XML-based Views, Clean Architecture, MVVM)
**Package:** `com.blank`
**Module root:** this directory

---

## Architecture

Clean Architecture with strict layer boundaries:

```
:feature:* / :app   ←  Presentation (Fragment, ViewModel, Adapter)
        ↓
   :domain           ←  Pure Kotlin (Repository interfaces, Models, UseCases)
        ↑
    :data            ←  Repository impls, Retrofit, Room DAOs, DTOs, Mappers
```

**Module graph:**
```
:app → :core, :data, :domain, :navigation, :feature:home, :feature:splash
:feature:home, :feature:bookmark, :feature:explore → (via plugin) :core, :domain
:data → :domain
:core, :domain → (leaf)
```

---

## Tech Stack (locked versions — do NOT upgrade)

| Category        | Library              | Version  |
|-----------------|----------------------|----------|
| Language        | Kotlin               | 2.3.10   |

| Build           | AGP                  | 9.0.1    |
| Build           | KSP                  | 2.3.5    |
| DI              | Hilt                 | 2.59.2   |
| Networking      | Retrofit             | 2.11.0   |
| Networking      | OkHttp               | 4.12.0   |
| Local DB        | Room                 | 2.8.4    |
| Paging          | Paging 3             | 3.4.1    |
| UI              | Material Components  | 1.13.0   |
| Navigation      | Navigation Component | 2.9.7    |
| Image Loading   | Coil                 | 2.7.0    |
| Testing         | MockK                | 1.14.9   |
| Testing         | JUnit 4              | 4.13.2   |
| Testing         | Turbine (Flow test)  | 1.2.0    |

---

## Convention Plugins (apply these, never raw AGP)

| Plugin                         | Use case                          |
|--------------------------------|-----------------------------------|
| `buildlogic.android.application` | `:app` only                     |
| `buildlogic.android.library`   | `:core`, `:data`, `:domain`, `:navigation` |
| `buildlogic.android.feature`   | All `:feature:*` modules          |
| `buildlogic.android.room`      | Any module using Room (`:data`)   |

The `buildlogic.android.feature` plugin **auto-injects** `:core` and `:domain` — do not re-declare them.

---

## Layer Boundary Rules (HARD — violations break PR)

### Domain (`:domain`)
- ZERO `android.*` imports
- ZERO framework classes (`Context`, `Application`, `LiveData`)
- Repository interfaces here; implementations in `:data`
- Error propagation: `Resource<T>` only — never `throw` across boundaries

### Data (`:data`)
- Only implements `:domain` interfaces
- DTOs + Room entities stay here; map to domain models at repository boundary
- ALL Retrofit calls use the `NetworkResponse<T, E>` sealed class pattern
- ALL Room operations on injected `CoroutineDispatcher` — never hardcode `Dispatchers.IO`
- Mapper pattern: `BaseListMapper<DTO, DomainModel>` from `:domain`

### Presentation (`:feature:*`)
- ViewModels extend `BaseViewModel`, use `launchWithLoading()`
- Expose `StateFlow<UiState<T>>` — never `MutableStateFlow` or `LiveData` to UI
- Collect flows in `observeState()` using `collectWithLifecycle()`
- `UiState` states: `Loading`, `Success(data)`, `Error(message)`, `Empty`
- Never pass `android.*` imports into `:domain` from feature modules

---

## Key Patterns

### SafeApiCall via NetworkResponse (existing pattern in :data)
```kotlin
// NetworkResponse is the sealed class wrapping Retrofit responses
// Used in PagingSource and repository impls
when (val response = newsApiService.getTopHeadlines(...)) {
    is NetworkResponse.Success    -> // response.data
    is NetworkResponse.ErrorApi   -> // response.code, response.error
    is NetworkResponse.ErrorNetwork -> // response.error (IOException)
    is NetworkResponse.ErrorUnknown -> // response.error
}
```

### PagingSource pattern
```kotlin
class XxxPagingSource(...) : PagingSource<Int, DomainModel>() {
    companion object {
        const val PAGE_SIZE = 5          
        private const val STARTING_PAGE = 1
    }
    
}
```

### Room Entity + DAO pattern
```kotlin
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String,
    // fields...
    val isBookmarked: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE isTopHeadline = 1")
    fun getTopHeadlines(): Flow<List<ArticleEntity>>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)
}
```

### Offline-First Top Headlines pattern
- Network fetch → insert to Room → UI observes Room `Flow<List<ArticleEntity>>`
- On no network: Room Flow still emits last cached data
- Pull-to-refresh: forces network re-fetch + Room upsert

### Bookmark Consistency pattern
- Single source of truth: `BookmarkDao.getAllBookmarks(): Flow<List<ArticleEntity>>`
- Bookmark state in `PagingData` via `combine()` with bookmark IDs `Set<String>`
- All lists (Top Headlines, Search, Bookmark) react to same DB `Flow`

---

## Navigation

Fragment-based Navigation Component, single `nav_graph.xml` in `:app`.
Deep link URI pattern: `app://newsapp/{destination}`

| Destination      | URI                        |
|------------------|----------------------------|
| Home             | `app://newsapp/home`       |
| Search/Explore   | `app://newsapp/explore`    |
| Bookmarks        | `app://newsapp/bookmarks`  |
| Detail           | safe args (intra-feature)  |

---

## File Naming & Package Conventions

| Type                | Pattern                    | Example                     |
|---------------------|----------------------------|-----------------------------|
| UseCase             | `{Verb}{Noun}UseCase`      | `GetBookmarksUseCase`       |
| Repository interface| `{Noun}Repository`         | `BookmarkRepository`        |
| Repository impl     | `{Noun}RepositoryImpl`     | `BookmarkRepositoryImpl`    |
| ViewModel           | `{Feature}ViewModel`       | `ExploreViewModel`          |
| Fragment            | `{Feature}Fragment`        | `ExploreFragment`           |
| DTO                 | `{Noun}Dto`                | `ArticleDto`                |
| Entity              | `{Noun}Entity`             | `ArticleEntity`             |
| Mapper              | `{Noun}Mapper`             | `ArticleEntityMapper`       |
| DAO                 | `{Noun}Dao`                | `ArticleDao`                |
| Paging source       | `{Noun}PagingSource`       | `SearchPagingSource`        |

---

## DI Rules

- `@Singleton` for network stack and repository bindings
- `@ViewModelScoped` for ViewModel-level dependencies
- `NetworkModule` in `:data` — provides OkHttp, Retrofit, NewsApiService
- `DatabaseModule` in `:data` — provides AppDatabase, ArticleDao, BookmarkDao
- `RepositoryModule` in `:data` — `@Binds` all repository interfaces

---

## API Config

```properties
# gradle.properties (never commit real key)
BASE_URL = "https://newsapi.org/v2/"
NEWS_API_KEY = "your_api_key_here"
```

Endpoints:
- Top Headlines: `GET /top-headlines?country=us&category=technology&pageSize=5&page={n}`
- Search: `GET /everything?q={query}&pageSize=5&page={n}` (no `country` param)

---

## Testing Strategy

- Unit tests: `src/test/` — MockK + JUnit4 + Turbine
- Instrumentation: `src/androidTest/` — Espresso
- Test naming: `{ClassUnderTest}Test.kt`
- All ViewModel tests use `TestCoroutineDispatcher` / `UnconfinedTestDispatcher`

---

## Kotlin Rules

- Prefer `val` over `var`; justify every `var`
- No `!!` — use `?: return`, `?: throw`, or `requireNotNull("reason")`
- Inject `CoroutineDispatcher` — never hardcode `Dispatchers.IO` or `Dispatchers.Main`
- `data class` for models; `sealed class`/`sealed interface` for states
- Expression bodies for single-expression functions
