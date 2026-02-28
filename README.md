# NewsApp

A modular Android news application built with Clean Architecture and MVVM. Designed for team development with strict layer separation, convention plugins for consistent build configuration, and a feature-module structure that scales as the codebase grows.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Module Responsibilities](#module-responsibilities)
- [Tech Stack](#tech-stack)
- [Convention Plugins](#convention-plugins)
- [Getting Started](#getting-started)
- [Base Classes Reference](#base-classes-reference)
- [Navigation Pattern](#navigation-pattern)
- [Code Style and Rules](#code-style-and-rules)

---

## Architecture Overview

NewsApp follows **Clean Architecture** with three distinct layers. Dependencies always point inward — outer layers depend on inner layers, never the reverse.

```
+--------------------------------------------------+
|                  Presentation                     |
|  (feature:home, feature:splash, app)              |
|  Activities, Fragments, ViewModels, UI State      |
+--------------------------------------------------+
                        |
                        v
+--------------------------------------------------+
|                    Domain                         |
|  Pure Kotlin — ZERO Android/platform imports      |
|  Repository interfaces, Models, Mappers           |
+--------------------------------------------------+
                        ^
                        |
+--------------------------------------------------+
|                     Data                          |
|  Repository implementations, Retrofit services    |
|  Room DAOs, DTOs, NetworkModule (DI)              |
+--------------------------------------------------+
```

### Module Structure

```
NewsApp/
├── app/                    # Application entry point
├── core/                   # Shared UI utilities, base classes, extensions
├── data/                   # Data layer — network, local DB, repository impls
├── domain/                 # Domain layer — pure Kotlin, no Android deps
├── navigation/             # Navigation module
├── feature/
│   ├── home/               # Home feature
│   ├── explore/            #Explore feature
│   └── bookmark/           #bBookmark screen feature
│   └── splash/             # Splash screen feature
└── build-logic/            # Gradle convention plugins
```

### Module Dependency Graph

```
:app ──────────────────────────────> :core
  |                                   :data
  |                                   :domain
  |                                   :navigation
  |                                   :feature:home
  |                                   :feature:explore
  |                                   :feature:bookmark
  └──────────────────────────────────> :feature:splash

:feature:* ────── (via convention plugin) ──> :core
                                               :domain

:data ─────────────────────────────> :domain

:core ──────────────── (leaf)
:domain ────────────── (leaf)
```

---

## Module Responsibilities

| Module         | Owns                                                                                                            | Must NOT Contain |
|----------------|-----------------------------------------------------------------------------------------------------------------|---|
| `:app`         | `MainApp` (Hilt entry point), `MainActivity`, top-level `nav_graph.xml`, app-level DI wiring                    | Business logic, domain models, direct API calls |
| `:core`        | `BaseActivity`, `BaseFragment`, `BaseViewModel`, `UiState`, View/Flow extensions                                | Feature-specific logic, network or DB code, domain models |
| `:domain`      | `BaseMapper`, `Resource<T>`, Repository interfaces, domain models                                               | Android imports (`android.*`), framework classes, DTOs, DB entities |
| `:data`        | Repository implementations, Retrofit API services, Room DAOs and entities, DTOs, `NetworkModule`, `SafeApiCall` | UI logic, ViewModels, domain interfaces (only implements them) |
| `:navigation`  | Navigation constants, deep link URI definitions                                                                 | Feature logic, UI code |
| `:feature:*`   | `**Fragment`, `**ViewModel`, UI state and adapters                                                              | Direct data source access, domain model definitions |
| `build-logic/` | Gradle convention plugins for consistent build setup                                                            | Runtime application code |

---

## Tech Stack

| Category | Library | Version |
|---|---|---|
| Build | Android Gradle Plugin | 9.0.1 |
| Build | Gradle | 9.2.1 |
| Language | Kotlin | 2.3.10 |
| Language | KSP (Kotlin Symbol Processing) | 2.3.5 |
| DI | Hilt | 2.59.2 |
| Networking | Retrofit | 2.11.0 |
| Networking | OkHttp | 4.12.0 |
| Local DB | Room | 2.8.4 |
| Paging | Paging 3 | 3.4.1 |
| UI | Material Components | 1.13.0 |
| Navigation | Navigation Component | 2.9.7 |
| Debug | Chucker (HTTP inspector) | 4.1.0 |
| UI | Splash Screen API | 1.0.1 |
| Serialization | Kotlinx Serialization | 1.7.3 |
| Testing | MockK | 1.14.9 |
| Testing | JUnit 4 | 4.13.2 |

---

## Convention Plugins

Convention plugins live in `build-logic/` and enforce consistent build configuration across all modules. Apply them in a module's `build.gradle.kts` instead of configuring AGP directly.

### `buildlogic.android.application`

For the `:app` module only.

- `compileSdk` 36, `minSdk` 26, `targetSdk` 36
- `sourceCompatibility` / `targetCompatibility` Java 21
- Enables `viewBinding`
- Applies `com.android.application` (Kotlin auto-applied by AGP 9)

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.buildlogic.android.application)
}
```

### `buildlogic.android.library`

For all library modules (`:core`, `:data`, `:domain`, `:navigation`).

- `compileSdk` 36, `minSdk` 26
- Java 21 toolchain
- Applies `com.android.library` (Kotlin auto-applied by AGP 9)

```kotlin
// domain/build.gradle.kts
plugins {
    alias(libs.plugins.buildlogic.android.library)
}
```

### `buildlogic.android.feature`

For all `feature/*` modules. Extends `buildlogic.android.library` and adds:

- `viewBinding` enabled
- Auto-includes `:core` and `:domain` as `implementation` dependencies
- Common dependencies pre-wired: Material3, Navigation Component, Lifecycle ViewModel/Runtime, Fragment KTX, AppCompat, ConstraintLayout

```kotlin
// feature/home/build.gradle.kts
plugins {
    alias(libs.plugins.buildlogic.android.feature)
}
// No need to manually add :core, :domain, or common deps — the plugin handles it.
```

### `buildlogic.android.room`

For any module that uses Room (currently `:data`).

- Applies `androidx.room` and `com.google.devtools.ksp`
- Adds `room-runtime`, `room-ktx`, `room-compiler` (via KSP), `room-testing`
- Configures Room schema export to `$projectDir/schemas`

```kotlin
// data/build.gradle.kts
plugins {
    alias(libs.plugins.buildlogic.android.library)
    alias(libs.plugins.buildlogic.android.room)
}
```

---

## Getting Started

### Prerequisites

- Android Studio Meerkat (2024.3.1) or later
- JDK 21
- Android SDK with API 36

### Setup

**1. Clone the repository**

```bash
git clone https://github.com/your-org/newsapp.git
cd newsapp
```

**2. Configure API credentials**

Open `gradle.properties` in the project root and set:

```properties
BASE_URL = "https://newsapi.org/v2/"
NEWS_API_KEY = "your_api_key_here"
```

Both values are injected into `BuildConfig` by the `:data` module. Never commit real keys. Use `gradle.properties.example` as a template for the team.

**3. Sync and build**

```bash
./gradlew assembleDebug
```

**4. Run tests**

```bash
./gradlew test
```

**5. Run on device or emulator**

```bash
./gradlew installDebug
```

---

## Base Classes Reference

### Presentation Layer (`:core`)

**`BaseActivity<VB : ViewBinding>(inflate: (LayoutInflater) -> VB)`**

Handles ViewBinding inflation and cleanup. Subclasses pass the binding inflate function and implement `onViewReady()` instead of working directly in `onCreate()`.

```kotlin
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onViewReady(savedInstanceState: Bundle?) {
        // binding is safe to use here
    }
}
```

**`BaseFragment<VB : ViewBinding>(inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB)`**

ViewBinding is lifecycle-safe — inflated in `onCreateView()`, cleared in `onDestroyView()`. Provides two hooks:

- `onViewReady(savedInstanceState)` — called from `onViewCreated()`, set up views here
- `observeState()` — called after `onViewReady()`, collect flows here (open, not abstract)

```kotlin
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun onViewReady(savedInstanceState: Bundle?) {
        // setup views, click listeners, adapters
    }

    override fun observeState() {
        collectWithLifecycle(viewModel.articles) { state ->
            // react to state changes
        }
    }
}
```

**`BaseViewModel`**

MVVM-style base ViewModel with shared loading and error handling. Each ViewModel defines its own `StateFlow` per UI component — no single-state-object constraint.

```kotlin
class HomeViewModel @Inject constructor(
    private val repository: NewsRepository,
) : BaseViewModel() {

    private val _articles = MutableStateFlow<UiState<List<Article>>>(UiState.Loading)
    val articles: StateFlow<UiState<List<Article>>> = _articles.asStateFlow()

    private val _categories = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    val categories: StateFlow<UiState<List<Category>>> = _categories.asStateFlow()
  
}
```

**`UiState<T>`** — sealed interface with four states:

| State | When to use |
|---|---|
| `UiState.Loading` | Initial load or refresh in progress |
| `UiState.Success(data: T)` | Data available |
| `UiState.Error(message: String)` | Recoverable error with a user-facing message |
| `UiState.Empty` | Successful load with no data to display |

### Domain Layer (`:domain`)

**`Resource<T>`** — domain result wrapper:

```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    data class Exception(val throwable: Throwable) : Resource<Nothing>()
}
```

**`BaseMapper<FROM, TO>`** and **`BaseListMapper<FROM, TO>`** — interfaces for mapping DTOs to domain models:

```kotlin
interface BaseMapper<in FROM, out TO> {
    fun map(from: FROM): TO
}

interface BaseListMapper<in FROM, out TO> : BaseMapper<List<FROM>, List<TO>> {
    override fun map(from: List<FROM>): List<TO> = from.map { mapItem(it) }
    fun mapItem(from: FROM): TO
}

// Usage
class ArticleMapper : BaseListMapper<ArticleDto, Article> {
    override fun mapItem(from: ArticleDto) = Article(
        title = from.title.orEmpty(),
        description = from.description.orEmpty(),
    )
}
```

### Data Layer (`:data`)

**`NetworkResult<T>`** and **`safeApiCall`** — wraps Retrofit `Response<T>` for safe API calls:

```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>()
}

// Usage in repository
suspend fun getArticles(): Resource<List<Article>> {
    return when (val result = safeApiCall { apiService.getTopHeadlines() }) {
        is NetworkResult.Success -> Resource.Success(mapper.map(result.data.articles))
        is NetworkResult.Error -> Resource.Error(result.message, result.code)
        is NetworkResult.Exception -> Resource.Exception(result.throwable)
    }
}
```

**`NetworkModule`** — Hilt DI module providing the full networking stack:

- `OkHttpClient` with `ApiKeyInterceptor`, `ChuckerInterceptor` (debug), `HttpLoggingInterceptor`
- `Retrofit` configured with `BuildConfig.BASE_URL` and Gson converter
- `NewsApiService` created from Retrofit
- 30-second timeouts for connect/read/write

### Core Extensions (`:core`)

```kotlin
// Visibility
fun View.visible()    { visibility = View.VISIBLE }
fun View.gone()       { visibility = View.GONE }
fun View.invisible()  { visibility = View.INVISIBLE }

// Toast
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT)

// Navigation
fun NavController.navigateDeepLink(uri: String, navOptions: NavOptions? = null)
fun NavController.navigateWithArgs(@IdRes destinationId: Int, args: Bundle)
fun <T : Parcelable> NavController.navigateWithParcelable(@IdRes destinationId: Int, key: String, value: T)

// Lifecycle-safe Flow collection
fun <T> Fragment.collectWithLifecycle(
    flow: Flow<T>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit,
)
```

---

## Navigation Pattern

NewsApp uses **Fragment-based Navigation Component** with a single `nav_graph.xml` defined in `:app`. **All navigation uses deep link URIs** — no action-based navigation between fragments.

### Why Deep Links Only

In a multi-module setup, `:feature:home` and `:feature:bookmark` cannot reference each other's generated `R` or navigation action IDs — they have no compile-time dependency on each other. Deep link URIs are plain strings that resolve at runtime through the navigation graph.

Using deep links consistently eliminates cross-module coupling and makes navigation more testable (deep links can be triggered from notifications, external apps, etc.).

### Deep Link Convention

Deep links follow the pattern: `app://newsapp/{destination}`

| Destination | URI |
|---|---|
| Home | `app://newsapp/home` |
| Explore | `app://newsapp/explore` |
| Bookmarks | `app://newsapp/bookmarks` |
| Detail | `app://newsapp/detail` |

### Navigation Extensions (`:core`)

The `:core` module provides extension functions on `NavController`:

```kotlin
// Simple navigation (no arguments) — uses deep link
findNavController().navigateDeepLink("app://newsapp/home")

// Navigation with Parcelable argument — uses resource ID
findNavController().navigateWithParcelable(
    destinationId = R.id.detailArticleFragment,
    key = "newsItem",  // matches argument name in nav_graph.xml
    value = newsItem,
)

// Navigation with full Bundle
findNavController().navigateWithArgs(
    destinationId = R.id.detailArticleFragment,
    args = bundle,
)
```

**Why two approaches?**
- Deep links work for simple navigation and enable external URLs/testability
- Resource IDs are required for Parcelable arguments (deep links can't carry complex objects)
- Since `com.blank.core.R.id.*` is already accessible to all feature modules, using it for detail navigation doesn't add new coupling

### Registering a Deep Link in nav_graph.xml

```xml
<!-- app/src/main/res/navigation/nav_graph.xml -->
<fragment
    android:id="@+id/homeFragment"
    android:name="com.blank.feature.home.HomeFragment">
    <deepLink app:uri="app://newsapp/home" />
</fragment>

<fragment
    android:id="@+id/detailArticleFragment"
    android:name="com.blank.feature.home.DetailArticleFragment">
    <deepLink app:uri="app://newsapp/detail" />
    <argument
        android:name="newsItem"
        app:argType="com.blank.core.model.NewsItem" />
</fragment>
```

### Rules

1. **All destinations register deep links** — enables external navigation and testing
2. **Use navigation extensions** — call `navigateDeepLink()`, `navigateWithParcelable()`, or `navigateWithArgs()` from `:core`
3. **URI pattern consistent** — `app://newsapp/{destination}` for all destinations
4. **No `<action>` elements** — navigation goes through deep links or resource IDs directly

---

## Code Style and Rules

These rules are enforced during code review. PRs that violate layer boundaries will be rejected.

### Layer Boundary Rules

**Domain layer (`:domain`) — strictly pure Kotlin**

- Zero `android.*` imports — no exceptions.
- Zero framework classes (`Context`, `Application`, `LiveData`, etc.).
- Repository interfaces are defined here; implementations live in `:data`.
- Error propagation uses `Resource<T>` — never throw across layer boundaries.

**Data layer (`:data`)**

- Only `:domain` interfaces are implemented here — the domain layer must not be aware of data layer types.
- DTOs and Room entities stay inside `:data`. Map to domain models at the repository boundary using `BaseMapper`/`BaseListMapper`.
- Use `safeApiCall {}` for all Retrofit calls — never call `.execute()` or handle `Response<T>` directly.
- All network and DB operations run on injected dispatchers, never hardcoded `Dispatchers.IO`.

**Presentation layer (feature modules)**

- ViewModels extend `BaseViewModel` and use `launchWithLoading()` for coroutine operations.
- Expose `StateFlow` to the UI, not `MutableStateFlow` or `LiveData`.
- Collect flows in Fragments using `collectWithLifecycle()` inside `observeState()`.
- UI state is `UiState<T>` — never expose raw domain models directly to Views.

### Kotlin Conventions

- Prefer `val` over `var`. Mutability must be justified.
- Use `data class` for models, `sealed class` or `sealed interface` for states and results.
- Avoid `!!`. Use `?: return`, `?: throw`, or `requireNotNull(message)` with a meaningful message.
- Inject `CoroutineDispatcher` via Hilt — never hardcode `Dispatchers.IO` or `Dispatchers.Main`.
- Use expression bodies for simple single-expression functions.

### Naming Conventions

| Type | Convention | Example |
|---|---|---|
| UseCase | `{Verb}{Noun}UseCase` | `GetTopHeadlinesUseCase` |
| Repository interface | `{Noun}Repository` | `NewsRepository` |
| Repository impl | `{Noun}RepositoryImpl` | `NewsRepositoryImpl` |
| ViewModel | `{Feature}ViewModel` | `HomeViewModel` |
| UI State | `UiState<{Model}>` | `UiState<List<Article>>` |
| Fragment | `{Feature}Fragment` | `HomeFragment` |
| DTO | `{Noun}Dto` | `ArticleDto` |
| Mapper | `{Noun}Mapper` | `ArticleMapper` |

### DI Rules

- Hilt modules (`@Module`, `@InstallIn`) live in the module that owns the dependency:
  - `NetworkModule` in `:data`
  - Feature-specific providers in their respective feature module
- The API key is read from `BuildConfig.NEWS_API_KEY` — never hardcode credentials in source.
- Use `@Singleton` for network and repository bindings; use `@ViewModelScoped` for ViewModel-level dependencies.

---

## API Key Security

`NEWS_API_KEY` must never appear in version control. The recommended team setup:

1. Provide a `gradle.properties.example` with placeholder values.
2. Each developer creates their own `gradle.properties` locally.
3. CI/CD injects the key via environment variables or a secrets manager.
