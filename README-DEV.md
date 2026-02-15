# Blinko Android App

Technical documentation for the Blinko Android client.

## Architecture

The app follows **MVVM + Clean Architecture** with a feature-module architecture using `api`/`implementation` splits.

All feature code is organized into dedicated modules: `feature-auth`, `feature-notes`, `feature-search`, `feature-settings`, `feature-tags`. Shared infrastructure is provided by purpose-specific modules: `shared-domain`, `shared-networking`, `shared-storage`, `shared-theme`, `shared-ui`, and `shared-navigation`.

### Layers

```
UI (Compose) → ViewModel → Use Case → Repository Interface → Repository Impl → API Client
```

- **Presentation**: Jetpack Compose screens, ViewModels with `StateFlow`
- **Domain**: Use cases, domain models, repository interfaces (no Android dependencies)
- **Data**: Retrofit API clients, repository implementations, data mappers, local storage

### Data flow example

```
User action → ViewModel calls UseCase
  → UseCase calls Repository (interface in feature:api)
    → RepositoryImpl (in feature:implementation) calls ApiClient
      → Retrofit HTTP request
    → Returns ApiResult → mapped to BlinkoResult<T>
  → ViewModel updates StateFlow
→ Compose UI recomposes
```

## Module Structure

```
BlinkoAndroid/
├── app/                              # Application entry point, wires all modules
├── shared-domain/                    # BlinkoResult, BlinkoSession, BlinkoUser, AuthenticationRepository, LocalStorage interfaces
├── shared-networking/                # RetrofitModule, OkHttp setup, ApiResult, ApiResultExtensions
├── shared-storage/                   # LocalStorageSharedPreferences, LocalStorageModule
├── shared-theme/                     # Material3 colors, typography, theme
├── shared-ui/                        # BlinkoViewModel, BlinkoTextField, BlinkoPasswordField, BlinkoButton, TabBarComposable, Loading
├── shared-navigation/
│   ├── api/                          # Navigation interfaces (BlinkoNavigationRouter, BlinkoNavigators)
│   └── implementation/               # NavigationActivity, BlinkoNavigationController
├── feature-auth/
│   ├── api/                          # Public interfaces (AuthFactory, SessionUseCases)
│   └── implementation/               # Login screen, auth repository, DI module
├── feature-notes/
│   ├── api/                          # Public interfaces (NotesFactory, NoteRepository, NoteSearchUseCase, NoteListItem, domain models)
│   └── implementation/               # Note screens, ViewModels, use cases, API client, repository, DTOs, DI module
├── feature-search/
│   ├── api/                          # Public interfaces (SearchFactory)
│   └── implementation/               # Search screen, ViewModel, DI module
├── feature-settings/
│   ├── api/                          # Public interfaces (SettingsFactory, tab use cases)
│   └── implementation/               # Settings screen, preferences storage, DI module
└── feature-tags/
    ├── api/                          # Public interfaces
    └── implementation/               # Tag UI components, DI module
```

### Module dependency rules

- `app` depends on all `shared-*` modules and all `feature` modules. This is needed for hilt to work
- `shared-navigation:implementation` depends on `shared-domain`, `shared-theme`, `shared-ui`, and feature `api` modules (never `implementation`)
- `shared-networking` depends on `shared-domain`
- `shared-storage` depends on `shared-domain`
- `shared-ui` depends on `shared-domain` and `shared-theme`
- `feature:implementation` modules depend on their own `api`, `shared-domain`, `shared-networking` (if needed), `shared-storage` (if needed), `shared-theme`, `shared-ui`, and `shared-navigation:api`
- Feature `api` modules depend on `shared-domain` (for shared types like `BlinkoResult`) but not on other feature modules

This api/implementation split means feature internals are hidden from the rest of the app. Features expose only factory interfaces and use case contracts through their `api` module.

### Feature module pattern

Each feature follows the same structure:

```
feature-*/
├── api/
│   ├── *Factory.kt            # Composable factory interface
│   ├── *EntryPoint.kt         # Hilt entry point for accessing the factory
│   └── domain/                # Public use case interfaces (if needed)
└── implementation/
    ├── presentation/
    │   ├── *ScreenComposable.kt
    │   ├── *ScreenViewModel.kt
    │   └── *FactoryImpl.kt     # Factory implementation providing composables
    ├── data/                    # Feature-specific repositories and API clients
    ├── domain/                  # Use case implementations
    └── di/
        └── *Module.kt          # Hilt module binding interfaces to implementations
```

## Shared Modules

### shared-domain

Pure Kotlin module (`com.github.pepitoria.blinkoapp.shared.domain`). Contains shared domain types:
- `BlinkoResult<T>` — sealed result type (Success/Error)
- `BlinkoSession`, `BlinkoUser` — auth/session models
- `AuthenticationRepository` — auth contract interface
- `LocalStorage` — persistence interface

### shared-networking

Networking infrastructure (`com.github.pepitoria.blinkoapp.shared.networking`). Depends on `shared-domain`:
- `RetrofitModule` — Retrofit 3 + OkHttp 5 setup, logging interceptor, 30s timeouts (Hilt singleton)
- `ApiResult<T>` — generic API response wrapper (ApiSuccess / ApiErrorResponse)
- `ApiResultExtensions` — `toBlinkoResult()` extension for mapping ApiResult to BlinkoResult

### shared-storage

Local persistence (`com.github.pepitoria.blinkoapp.shared.storage`). Depends on `shared-domain`:
- `LocalStorageSharedPreferences` — `LocalStorage` implementation using SharedPreferences
- `LocalStorageModule` — Hilt module binding LocalStorage to SharedPreferences impl

### shared-theme

Material Design theming (`com.github.pepitoria.blinkoapp.shared.theme`):
- `Color.kt` — Material3 color definitions
- `Type.kt` — Typography configuration
- `Theme.kt` — BlinkoTheme composable

### shared-ui

Reusable UI components (`com.github.pepitoria.blinkoapp.shared.ui`). Depends on `shared-domain` and `shared-theme`:
- `BlinkoViewModel` — base ViewModel with lifecycle awareness (`onStart`/`onStop`)
- `BlinkoTextField`, `BlinkoPasswordField`, `BlinkoButton` — custom Material3 components
- `TabBarComposable` — bottom navigation tab bar
- `Loading` — loading indicator composable

### shared-navigation

Navigation infrastructure (`com.github.pepitoria.blinkoapp.shared.navigation`). Split into api/implementation:
- **api**: `BlinkoNavigationRouter`, `BlinkoNavigators` — navigation interfaces and extensions
- **implementation**: `NavigationActivity` (hosts Compose NavHost), `BlinkoNavigationController`

## Navigation

Uses Jetpack Compose Navigation with two nav graphs:

```
Auth graph:
  └── auth/login

Home graph:
  ├── home/blinko-list       (Blinkos tab)
  ├── home/note-list          (Notes tab)
  ├── home/todo-list          (Todos tab)
  ├── home/search             (Search tab)
  ├── home/note-edit/{noteId} (Edit/create note)
  └── home/settings           (Settings)
```

Navigation helpers are defined as extension functions on `NavHostController` in `BlinkoNavigators.kt`.

## Dependency Injection

**Hilt 2.57.1** with KSP.

Key modules:
- `RetrofitModule` (shared-networking) — provides OkHttpClient and Retrofit instance (singleton scope)
- `LocalStorageModule` (shared-storage) — binds LocalStorage to SharedPreferences implementation
- `NavigationModule` (shared-navigation:implementation) — binds navigation controller
- `NotesModule` (feature-notes:implementation) — provides NotesApi, NotesApiClient (flavor-aware), binds NoteRepository and NotesFactory
- `AuthModule` (feature-auth:implementation) — binds auth factory, session use cases, and auth repository
- `SearchModule` (feature-search:implementation) — binds search factory
- `SettingsModule` (feature-settings:implementation) — binds settings factory and use cases

ViewModels use `@HiltViewModel` with constructor injection.

## Build Configuration

### Flavors

| Flavor      | Purpose                                  |
|-------------|------------------------------------------|
| `remote`    | Production — connects to real Blinko API |
| `mockLocal` | Development — uses fake API client       |

### Build types

| Type      | Notes                                  |
|-----------|----------------------------------------|
| `debug`   | Debuggable, app name suffix ".debug"   |
| `release` | Minification disabled, signed via external properties |

### SDK targets

- **compileSdk / targetSdk**: 36
- **minSdk**: 25
- **JVM target**: 17

### Custom Gradle tasks

- `newRelease` — builds APK and increments version code
- `uploadApkToGitHub` — uploads APK to GitHub releases via API

## Key Dependencies

| Library                  | Version   | Purpose                          |
|--------------------------|-----------|----------------------------------|
| Jetpack Compose BOM      | 2026.01.01| UI framework                     |
| Material3                | (BOM)    | Design system                    |
| Hilt                     | 2.57.1   | Dependency injection             |
| Retrofit                 | 3.0.0    | HTTP client                      |
| OkHttp                   | 5.3.2    | HTTP engine                      |
| Kotlin                   | 2.2.20   | Language                         |
| Timber                   | 5.0.1    | Logging                          |
| richtext-commonmark      | 1.0.0-alpha03 | Markdown rendering          |
| Accompanist FlowLayout   | 0.36.0   | Tag layout                       |

## Testing

### Stack

- **Framework**: JUnit Jupiter (JUnit 5)
- **Mocking**: MockK 1.14.9
- **Coroutine testing**: kotlinx-coroutines-test 1.10.2
- **Android instrumentation**: AndroidJUnit4 + Espresso

### Test coverage

All testable business logic classes are covered (~100% of target classes):

| Module | Test Location | Classes Tested |
|--------|---------------|----------------|
| feature-auth | `implementation/src/test/` | SessionUseCasesImpl, AuthenticationRepositoryImpl, LoginScreenViewModel, UserMapper |
| feature-notes | `implementation/src/test/` | NoteRepositoryApiImpl, NoteListUseCase, NoteDeleteUseCase, NoteUpsertUseCase, NoteListByIdsUseCase, NoteListScreenViewModel, NoteEditScreenViewModel, ShareAndEditWithBlinkoViewModel, NoteExtensions |
| feature-tags | `implementation/src/test/` | TagsRepositoryImpl, GetTagsUseCase, TagsListViewModel, TagMapper |
| feature-search | `implementation/src/test/` | SearchScreenViewModel |
| feature-settings | `implementation/src/test/` | TabsRepositoryImpl, DefaultTabUseCaseImpl, SettingsScreenViewModel |
| shared-networking | `src/test/` | ApiResultExtensions |
| shared-storage | `src/androidTest/` | LocalStorageSharedPreferences (instrumentation) |

### Running tests

```bash
# All unit tests (custom task)
./gradlew allUnitTests

# All instrumentation tests (requires emulator/device)
./gradlew allInstrumentationTests

# All tests (unit + instrumentation)
./gradlew allTests

# Unit tests (specific module)
./gradlew :feature-notes:implementation:testRemoteDebugUnitTest

# Instrumentation tests (specific module)
./gradlew :shared-storage:connectedRemoteDebugAndroidTest
```

### Test patterns

Tests follow a consistent pattern using Given/When/Then structure:

```kotlin
@Test
fun `method returns success when dependency succeeds`() = testScope.runTest {
    // Given
    coEvery { mockDependency.call() } returns Success(data)

    // When
    val result = sut.method()

    // Then
    assertIs<BlinkoResult.Success<Data>>(result)
    assertEquals(expected, result.data)
}
```

- **ViewModels**: Use `StandardTestDispatcher` with `Dispatchers.setMain()` for coroutine control
- **Repositories/UseCases**: Use `runTest` with `coEvery`/`coVerify` for suspend function mocking
- **Mappers**: Pure function tests with direct assertions

## Migration Status

### Feature extraction — completed

| Feature         | Extracted from core | api/impl split |
|-----------------|---------------------|----------------|
| Authentication  | Yes                 | Yes            |
| Notes           | Yes                 | Yes            |
| Search          | Yes                 | Yes            |
| Settings        | Yes                 | Yes            |
| Tags            | Yes                 | Yes            |

All feature code has been extracted from core. The core modules now contain only shared infrastructure (networking, theme, navigation, base classes).

### Shared module extraction — completed

All core modules have been fully decomposed into purpose-specific shared modules:
- `shared-domain` — domain types and interfaces
- `shared-networking` — Retrofit/OkHttp setup, API result types
- `shared-storage` — SharedPreferences-based local storage
- `shared-theme` — Material3 theming (colors, typography, theme)
- `shared-ui` — reusable UI components (BlinkoViewModel, custom fields, buttons, tab bar)
- `shared-navigation` — navigation infrastructure (api/implementation split)
