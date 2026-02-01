# Blinko Android App

Technical documentation for the Blinko Android client.

## Architecture

The app follows **MVVM + Clean Architecture** and is **mid-migration** from a monolithic 3-module layout (`core:data`, `core:domain`, `core:presentation`) to a feature-module architecture with `api`/`implementation` splits.

Features already extracted: `feature-auth`, `feature-search`, `feature-settings`, `feature-tags`. The `core` modules still contain note-related feature code (screens, use cases, repositories, API clients) that has not yet been migrated out.

The end goal is to **eliminate the `core` modules entirely**. All shared infrastructure will be split into purpose-specific `shared-*` modules (e.g. `shared-networking`, `shared-theme`, `shared-navigation`), and all remaining feature code will move into `feature-notes`.

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
  → UseCase calls Repository (interface in :core:domain)
    → RepositoryImpl (in :core:data) calls BlinkoApiClient
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
│   ├── data/                         # Networking (Retrofit/OkHttp), repositories, local storage
│   ├── domain/                       # Use cases, domain models, repository interfaces
│   └── presentation/                 # Shared UI: navigation, theme, base ViewModel, common screens
├── feature-auth/
│   ├── api/                          # Public interfaces (AuthFactory, SessionUseCases)
│   └── implementation/               # Login screen, auth repository, DI module
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

- `app` depends on all `core` modules and all `feature` modules
- `core:presentation` depends on `core:domain` and feature `api` modules (never `implementation`)
- `core:data` depends on `core:domain`
- `feature:implementation` modules depend on their own `api`, `core:domain`, `core:data`, and `core:presentation`
- Feature `api` modules have no dependencies on other feature modules

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

## Core Modules (legacy — being phased out)

The `core` modules are a remnant of the original 3-module architecture. They currently hold both shared infrastructure and note-related feature code. The plan is to dismantle them entirely: feature code moves to `feature-notes`, and shared infrastructure moves to dedicated `shared-*` modules.

### core:domain

Pure Kotlin module.

**Shared infrastructure** → will move to `shared-*` modules:
- `BlinkoResult<T>` — sealed result type (Success/Error) → `shared-domain`
- `BlinkoSession`, `BlinkoUser` — auth/session models → `shared-domain`
- `AuthenticationRepository` — auth contract → `shared-domain`
- `LocalStorage` — persistence interface → `shared-domain`

**Feature code** → will move to `feature-notes`:
- Use cases: `NoteListUseCase`, `NoteSearchUseCase`, `NoteUpsertUseCase`, `NoteDeleteUseCase`, `NoteListByIdsUseCase`
- `NoteRepository` interface
- `BlinkoNote`, `BlinkoNoteType` models

### core:data

**Shared infrastructure** → will move to `shared-*` modules:
- `RetrofitModule` — Retrofit 3 + OkHttp 5 setup, logging interceptor, 30s timeouts → `shared-networking`
- `BlinkoApiModule` — flavor-aware API client selection → `shared-networking`
- `LocalStorageSharedPreferences` — `LocalStorage` implementation → `shared-storage`
- `ApiResult<T>` — generic API response wrapper → `shared-networking`
- `ApiResultExtensions` — shared result mapping → `shared-networking`

**Feature code** → will move to `feature-notes`:
- `BlinkoApiClient` interface and its implementations (`BlinkoApiClientNetImpl`, `BlinkoLocalFakesApiClientImpl`)
- `NoteRepositoryApiImpl`
- Note DTOs: `NoteResponse`, `NoteListRequest`, `NoteListByIdsRequest`, `UpsertRequest`, `DeleteNoteRequest`
- Note mappers: `NoteExtensions`

### core:presentation

**Shared infrastructure** → will move to `shared-*` modules:
- `BlinkoNavigationRouter`, `BlinkoNavigationController`, `BlinkoNavigators` → `shared-navigation`
- `NavigationActivity` (hosts Compose NavHost) → `shared-navigation`
- `BlinkoViewModel` with lifecycle awareness (`onStart`/`onStop`) → `shared-ui`
- Material3 colors, typography, custom components (`BlinkoTextField`, `BlinkoPasswordField`, `BlinkoButton`) → `shared-theme`
- `TabBarComposable`, `Loading` → `shared-ui`

**Feature code** → will move to `feature-notes`:
- `NoteListScreenComposable` + `NoteListScreenViewModel`
- `NoteEditScreenComposable` + `NoteEditScreenViewModel`
- `ShareAndEditWithBlinkoActivity` + `ShareAndEditWithBlinkoViewModel`

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
- `RetrofitModule` — provides OkHttpClient and Retrofit instance (singleton scope)
- `BlinkoApiModule` — provides the correct `BlinkoApiClient` based on build flavor
- `RepositoryModule` — binds `NoteRepository` to its implementation
- `AuthModule` — binds auth factory, session use cases, and auth repository
- `SearchModule` — binds search factory
- `SettingsModule` — binds settings factory and use cases

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

Unit tests exist in `core:data` (e.g., `NoteRepositoryApiImplTest`). Tests use `runTest` for coroutine testing and `coEvery`/`coVerify` from MockK.

## Migration Status

### Completed

| Feature         | Extracted from core | api/impl split |
|-----------------|---------------------|----------------|
| Authentication  | Yes                 | Yes            |
| Search          | Yes                 | Yes            |
| Settings        | Yes                 | Yes            |
| Tags            | Yes                 | Yes            |

### Pending

| Component                        | Current location     | Target                         |
|----------------------------------|----------------------|--------------------------------|
| Note list screen + ViewModel     | core:presentation    | feature-notes:implementation   |
| Note edit screen + ViewModel     | core:presentation    | feature-notes:implementation   |
| Share-with-Blinko activity + VM  | core:presentation    | feature-notes:implementation   |
| Note use cases (5)               | core:domain          | feature-notes:implementation   |
| NoteRepository interface         | core:domain          | feature-notes:api              |
| BlinkoNote, BlinkoNoteType       | core:domain          | feature-notes:api              |
| NoteRepositoryApiImpl            | core:data            | feature-notes:implementation   |
| BlinkoApiClient + impls          | core:data            | feature-notes:implementation   |
| Note DTOs and mappers            | core:data            | feature-notes:implementation   |

Once all steps are complete, the `core` directory will be deleted entirely.

### Target module structure

```
BlinkoAndroid/
├── app/
├── shared-domain/                    # BlinkoResult, BlinkoSession, BlinkoUser, repository interfaces
├── shared-networking/                # RetrofitModule, OkHttp setup, ApiResult, ApiResultExtensions
├── shared-storage/                   # LocalStorage interface + SharedPreferences implementation
├── shared-navigation/                # Routes, NavHost, NavigationActivity, navigators
├── shared-theme/                     # Colors, Typography, Material3 theme
├── shared-ui/                        # BlinkoViewModel, BlinkoTextField, BlinkoButton, TabBar, Loading
├── feature-auth/
│   ├── api/
│   └── implementation/
├── feature-notes/
│   ├── api/
│   └── implementation/
├── feature-search/
│   ├── api/
│   └── implementation/
├── feature-settings/
│   ├── api/
│   └── implementation/
└── feature-tags/
    ├── api/
    └── implementation/
```

## TODO/Roadmap:

### 1. Extract `feature-notes` from core

- [ ] Create `feature-notes/api` module
  - [ ] Move `NoteRepository` interface, `BlinkoNote`, `BlinkoNoteType`
  - [ ] Create `NotesFactory` and `NotesEntryPoint` (following existing feature pattern)
- [ ] Create `feature-notes/implementation` module
  - [ ] Move note use cases (`NoteListUseCase`, `NoteSearchUseCase`, `NoteUpsertUseCase`, `NoteDeleteUseCase`, `NoteListByIdsUseCase`)
  - [ ] Move `NoteRepositoryApiImpl`
  - [ ] Move `BlinkoApiClient` interface + implementations (`BlinkoApiClientNetImpl`, `BlinkoLocalFakesApiClientImpl`)
  - [ ] Move note DTOs (`NoteResponse`, `NoteListRequest`, `NoteListByIdsRequest`, `UpsertRequest`, `DeleteNoteRequest`)
  - [ ] Move note mappers (`NoteExtensions`)
  - [ ] Move `NoteListScreenComposable` + `NoteListScreenViewModel`
  - [ ] Move `NoteEditScreenComposable` + `NoteEditScreenViewModel`
  - [ ] Move `ShareAndEditWithBlinkoActivity` + `ShareAndEditWithBlinkoViewModel`
  - [ ] Create `NotesModule` Hilt DI module
- [ ] Update `core:presentation` navigation controller to use `NotesFactory`
- [ ] Move `NoteRepositoryApiImplTest` to `feature-notes`

### 2. Split `core:data` into shared modules

- [ ] Create `shared-networking` module
  - [ ] Move `RetrofitModule`, `BlinkoApiModule`, `ApiResult`, `ApiResultExtensions`
- [ ] Create `shared-storage` module
  - [ ] Move `LocalStorage` interface (from `core:domain`) and `LocalStorageSharedPreferences` + `LocalStorageModule` (from `core:data`)
- [ ] Update all feature modules to depend on `shared-networking` / `shared-storage` instead of `core:data`
- [ ] Delete `core:data`

### 3. Split `core:domain` into shared modules

- [ ] Create `shared-domain` module
  - [ ] Move `BlinkoResult`, `BlinkoSession`, `BlinkoUser`, `AuthenticationRepository`
- [ ] Update all feature modules to depend on `shared-domain` instead of `core:domain`
- [ ] Delete `core:domain`

### 4. Split `core:presentation` into shared modules

- [ ] Create `shared-theme` module
  - [ ] Move `Color.kt`, `Type.kt`, `Theme.kt`
- [ ] Create `shared-ui` module
  - [ ] Move `BlinkoViewModel`, `BlinkoTextField`, `BlinkoPasswordField`, `BlinkoButton`, `TabBarComposable`, `Loading`
- [ ] Create `shared-navigation` module
  - [ ] Move `BlinkoNavigationRouter`, `BlinkoNavigationController`, `BlinkoNavigators`, `NavigationActivity`
- [ ] Update all feature modules to depend on `shared-theme` / `shared-ui` / `shared-navigation` instead of `core:presentation`
- [ ] Delete `core:presentation`

### 5. Cleanup

- [ ] Remove `core` directory
- [ ] Update `settings.gradle.kts` to remove all `:core:*` includes
- [ ] Verify build and tests pass across all flavors and build types

