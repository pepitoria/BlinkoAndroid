# Blinko Android App

Technical documentation for the Blinko Android client.

## Architecture

The app follows **MVVM + Clean Architecture** and is **mid-migration** from a monolithic 3-module layout to a feature-module architecture with `api`/`implementation` splits.

All feature code has been extracted: `feature-auth`, `feature-notes`, `feature-search`, `feature-settings`, `feature-tags`. The former `core:data` and `core:domain` modules have been replaced by three dedicated shared modules: `shared-domain`, `shared-networking`, and `shared-storage`. Only `core:presentation` remains, containing shared UI components, theme, and navigation.

The end goal is to **eliminate `core:presentation` entirely** by splitting it into `shared-theme`, `shared-ui`, and `shared-navigation` modules.

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
├── core/
│   └── presentation/                 # Shared: navigation, theme, base ViewModel, tab bar, common UI
├── shared-domain/                    # BlinkoResult, BlinkoSession, BlinkoUser, AuthenticationRepository, LocalStorage interfaces
├── shared-networking/                # RetrofitModule, OkHttp setup, ApiResult, ApiResultExtensions
├── shared-storage/                   # LocalStorageSharedPreferences, LocalStorageModule
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

- `app` depends on `core:presentation`, all `shared-*` modules, and all `feature` modules
- `core:presentation` depends on `shared-domain` and feature `api` modules (never `implementation`)
- `shared-networking` depends on `shared-domain`
- `shared-storage` depends on `shared-domain`
- `feature:implementation` modules depend on their own `api`, `shared-domain`, `shared-networking` (if needed), `shared-storage` (if needed), and `core:presentation`
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

## Core Module (remaining — being phased out)

### core:presentation

Shared UI components and navigation. Will be split into `shared-theme`, `shared-ui`, and `shared-navigation`:
- `BlinkoNavigationRouter`, `BlinkoNavigationController`, `BlinkoNavigators`
- `NavigationActivity` (hosts Compose NavHost)
- `BlinkoViewModel` with lifecycle awareness (`onStart`/`onStop`)
- Material3 colors, typography, custom components (`BlinkoTextField`, `BlinkoPasswordField`, `BlinkoButton`)
- `TabBarComposable`, `Loading`

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

- **Framework**: JUnit Jupiter (JUnit 5)
- **Mocking**: MockK 1.14.9
- **Coroutine testing**: kotlinx-coroutines-test 1.10.2
- **UI testing**: Espresso + Compose test utilities

Unit tests exist in `feature-notes:implementation` (e.g., `NoteRepositoryApiImplTest`). Tests use `runTest` for coroutine testing and `coEvery`/`coVerify` from MockK.

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

`core:data` and `core:domain` have been fully decomposed into:
- `shared-domain` — domain types and interfaces
- `shared-networking` — Retrofit/OkHttp setup, API result types
- `shared-storage` — SharedPreferences-based local storage

Both `core:data` and `core:domain` directories have been deleted.

### Remaining — split core:presentation into shared modules

`core:presentation` still needs to be decomposed into purpose-specific `shared-*` modules. Once complete, the `core` directory will be deleted entirely.

## TODO/Roadmap:

### 1. Extract `feature-notes` from core — DONE

- [x] Create `feature-notes/api` module
  - [x] Move `NoteRepository` interface, `BlinkoNote`, `BlinkoNoteType`
  - [x] Create `NotesFactory` and `NotesEntryPoint` (following existing feature pattern)
  - [x] Move `NoteSearchUseCase` (public API used by feature-search)
  - [x] Move `NoteListItem` composable (shared UI used by feature-search)
- [x] Create `feature-notes/implementation` module
  - [x] Move note use cases (`NoteListUseCase`, `NoteUpsertUseCase`, `NoteDeleteUseCase`, `NoteListByIdsUseCase`)
  - [x] Move `NoteRepositoryApiImpl`
  - [x] Move `NotesApiClient` interface + implementations (`NotesApiClientNetImpl`, `NotesLocalFakesApiClientImpl`)
  - [x] Move note DTOs (`NoteResponse`, `NoteListRequest`, `NoteListByIdsRequest`, `UpsertRequest`, `DeleteNoteRequest`)
  - [x] Move note mappers (`NoteExtensions`)
  - [x] Move `NoteListScreenComposable` + `NoteListScreenViewModel`
  - [x] Move `NoteEditScreenComposable` + `NoteEditScreenViewModel`
  - [x] Move `ShareAndEditWithBlinkoActivity` + `ShareAndEditWithBlinkoViewModel`
  - [x] Create `NotesModule` Hilt DI module
- [x] Update `core:presentation` navigation controller to use `NotesFactory`
- [x] Move `NoteRepositoryApiImplTest` to `feature-notes`
- [x] Update `feature-search` and `feature-auth` to depend on `feature-notes:api`
- [x] Remove all old note code from core modules
- [x] Remove `BlinkoApiModule`, `RepositoryModule`, and `provideBlinkoApi` from core:data

### 2. Split `core:data` and `core:domain` into shared modules — DONE

- [x] Create `shared-domain` module
  - [x] Move `BlinkoResult`, `BlinkoSession`, `BlinkoUser`, `AuthenticationRepository`, `LocalStorage`
  - [x] New package: `com.github.pepitoria.blinkoapp.shared.domain`
- [x] Create `shared-networking` module
  - [x] Move `RetrofitModule`, `ApiResult`, `ApiResultExtensions`
  - [x] New package: `com.github.pepitoria.blinkoapp.shared.networking`
- [x] Create `shared-storage` module
  - [x] Move `LocalStorageSharedPreferences`, `LocalStorageModule`
  - [x] New package: `com.github.pepitoria.blinkoapp.shared.storage`
- [x] Update all feature module `build.gradle.kts` to depend on `shared-*` instead of `core:data`/`core:domain`
- [x] Update all imports across ~25 consumer `.kt` files
- [x] Delete `core:data` and `core:domain`

### 3. Split `core:presentation` into shared modules

- [ ] Create `shared-theme` module — `Color.kt`, `Type.kt`, `Theme.kt`
- [ ] Create `shared-ui` module — `BlinkoViewModel`, `BlinkoTextField`, `BlinkoPasswordField`, `BlinkoButton`, `TabBarComposable`, `Loading`
- [ ] Create `shared-navigation` module — `BlinkoNavigationRouter`, `BlinkoNavigationController`, `BlinkoNavigators`, `NavigationActivity`
- [ ] Update all feature modules to depend on `shared-theme` / `shared-ui` / `shared-navigation` instead of `core:presentation`
- [ ] Delete `core:presentation`

### 4. Cleanup

- [ ] Remove `core` directory (after core:presentation is split)
- [ ] Update `settings.gradle.kts` to remove `:core:presentation`
- [ ] Verify build and tests pass across all flavors and build types

