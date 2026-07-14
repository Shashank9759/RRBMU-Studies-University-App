<style>
/* Ensure the markdown preview is scrollable both directions */
html, body { overflow: auto !important; height: auto; max-height: none; }
pre {
  overflow-x: auto !important;
  overflow-y: auto !important;
  max-width: 100%;
  white-space: pre;
  word-wrap: normal;
}
code { white-space: pre; }
table {
  display: block;
  overflow-x: auto;
  max-width: 100%;
  white-space: nowrap;
}
img { max-width: 100%; height: auto; }
</style>

# 05 — Architecture (Clean + MVVM + KMP)

This app uses **Clean Architecture** with three code layers, MVVM inside the
UI layer, Kotlin Multiplatform for cross-platform code, and Koin for dependency
injection. Below is the full picture.

---

## 1. The three layers

```
┌────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                      │
│                                                            │
│   Composables ── ViewModels ── UiState/Events              │
│      (Jetpack / Compose Multiplatform)                     │
│                                                            │
└─────────────────────────┬──────────────────────────────────┘
                          │  calls
                          ▼
┌────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                         │
│                                                            │
│      Models (Course, Paper, …)                             │
│      Repository interfaces (CourseRepository, …)           │
│      Use cases (GetCoursesUseCase, DownloadPaperUseCase…)  │
│                                                            │
└─────────────────────────┬──────────────────────────────────┘
                          │  implements
                          ▼
┌────────────────────────────────────────────────────────────┐
│                        DATA LAYER                          │
│                                                            │
│   Repository impls  ← Mappers ← DTOs                       │
│           │              │                                 │
│           ▼              ▼                                 │
│    Remote sources     Local sources                        │
│    (Firestore,        (SQLDelight,                         │
│     Storage, Auth,    multiplatform-settings,              │
│     Analytics)         PdfVaultFileStore)                  │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### Layer rules

- **UI depends on Domain (only)**. It never touches Firebase directly.
- **Data depends on Domain**. It provides implementations for the interfaces
  defined in Domain.
- **Domain depends on nothing**. Pure Kotlin. This is what keeps it testable
  and portable.

The layers map onto three Gradle modules: `composeApp`, `domain`, `data`
(see 03-project-structure.md).

---

## 2. MVVM inside the presentation layer

Each screen has a **ViewModel**. The ViewModel:

- Injects one or more use cases from the domain layer.
- Exposes a **StateFlow<UiState<T>>** (or a similar `StateFlow<Foo>`) for the
  Composable to collect.
- Optionally exposes a **SharedFlow** of one-shot events (snackbar messages,
  navigation triggers).
- Handles user actions as plain functions (`onSearchQueryChange(q)`,
  `refresh()`, `submit()`).

The Composable never contains business logic. It just calls
`viewModel.something()` and re-renders when the state emits.

### Generic UiState

`composeApp/src/commonMain/kotlin/com/studies/rrbmustudies/ui/state/UiState.kt`

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
```

Screens branch on this sealed type. The `Error.message` is typically produced
by `UserFacingErrors.kt`, which maps low-level exceptions to a human string.

---

## 3. Data flow — read example (Home screen)

```
User opens Home
      │
      ▼
HomeScreen collects HomeViewModel.state
      │
      ▼
HomeViewModel { init { load() } }
      │  combines flows from 4 use cases
      ▼
GetCoursesUseCase(), GetRecentPapersUseCase(),
GetActiveHomeAdsUseCase(), ObserveCarouselSettingsUseCase()
      │
      ▼
CourseRepositoryImpl / PaperRepositoryImpl / HomeAdRepositoryImpl
      │
      ▼
FirestoreCourseDataSource.observeCourses()
FirestorePaperDataSource.observeRecentPapers()
FirestoreHomeAdDataSource.observeActiveAds() / observeCarouselSettings()
      │
      ▼
GitLive Firestore SDK  ─►  Firebase Firestore
```

Everything above `HomeViewModel` is Compose. Everything below is coroutines +
Flow all the way down. The Composable re-renders automatically as Firestore
pushes new snapshots.

---

## 4. Data flow — write example (Download a paper)

```
User taps "Download offline"
      │
      ▼
PaperDetailViewModel.downloadToVault()
      │
      ▼
DownloadPaperToVaultUseCase(paper)
      │
      ▼
PdfVaultRepositoryImpl.downloadToVault(paper)
      │
      ├─► HTTP GET the pdfUrl (bytes)
      │
      ├─► PdfVaultFileStore.writeBytes(paperId, bytes)   ← platform-specific
      │
      └─► DownloadedPaperLocalDataSource.upsert(metadata)
                │
                ▼
             SQLDelight (RrbmuDatabase)
      │
      ▼
StateFlow<Boolean> isDownloaded emits `true`
      │
      ▼
UI shows "Open" button
```

Notice how the write is split into:
1. Fetch bytes over HTTP.
2. Write bytes to the platform-specific file store.
3. Persist metadata in the database.

Each step returns a `Result<T>` so the ViewModel can render a nice error toast.

---

## 5. Kotlin Multiplatform mechanics

### expect / actual

Files ending in `.kt` in `commonMain` are shared. Where the code *has* to touch
the platform (e.g. get a `Context`, write a file, ask for notification
permission), the code declares an `expect` function in commonMain and provides
`actual` implementations in `androidMain` and `iosMain`.

Examples used in this project:
- `PlatformServices.kt` — services like `openUrl(url)`, `shareText(text)`.
- `NotificationPermission.kt` — Compose helper to ask for notification permission.
- `PdfVaultFileStore.kt` — the local file store for downloaded PDFs.
- `AdMobBanner.kt` — an ad banner (Android renders it, iOS is a no-op).
- `PlatformPdfViewer.kt` — inside the PDF viewer screen, delegates to platform.
- Pickers — `ImagePicker`, `PdfPicker`, `NotificationAttachmentPicker`.

Full list is in **36-platform-specific.md**.

### iOS entry point

`iosMain/kotlin/.../MainViewController.kt` returns a `UIViewController` that
hosts the Compose `App()`. The Swift side calls this function to embed the
Kotlin UI.

### Android entry point

`androidMain/kotlin/.../MainActivity.kt` extends `ComponentActivity`, initialises
Koin, Firebase and AdMob, then calls `setContent { App(initialDeepLink) }`.

---

## 6. Dependency injection

Koin modules:

- `DataModule` — binds every `…RemoteDataSource` interface to its Firestore
  impl, every `…LocalDataSource` to its SQLDelight impl, and every
  `…Repository` interface to its `…RepositoryImpl`.
- `PlatformModule` — provides the SQLDelight driver, `ObservableSettings`
  instance, `PdfVaultFileStore` and other platform-scoped singletons
  (different on Android vs iOS).
- `DomainModule` — factories for every use case class.
- `PresentationModule` — every ViewModel.
- `AndroidAdsModule` (Android only) — binds `PaperAdGateway` to an AdMob-backed
  impl.

Kickoff: `shared/di/KoinInit.kt` bundles the common modules and accepts
`extraModules: List<Module>` for platform-only additions (like ads).

Full breakdown in **10-dependency-injection.md**.

---

## 7. Threading / dispatchers

- ViewModels use `viewModelScope`.
- Suspend calls to Firestore run on the IO dispatcher provided by the GitLive
  SDK.
- SQLDelight suspend queries use its `Dispatchers.IO` extension.
- UI code observes via `collectAsStateWithLifecycle()`, which is lifecycle-safe.

There is no manual thread management anywhere in `commonMain`.

---

## 8. Error handling

- Repository methods that can fail return `kotlin.Result<T>` (aliased via
  `domain/model/Result.kt`).
- Read Flows may swallow errors and fall back to defaults (e.g. Home ads
  fall back to `HomeDefaults.promoAds` if Firestore fails).
- ViewModels convert `Result.failure(e)` into `UiState.Error(message)` using
  `UserFacingErrors.kt`.
- No exception ever bubbles up to the Composable.

---

## 9. Testing strategy

- `domain/src/commonTest/kotlin/…/CourseSeedTest.kt` — unit tests for the
  bundled `CourseSeed`.
- `data/src/commonTest/kotlin/…/MappersTest.kt` — tests DTO ↔ domain mapping.
- `data/src/commonTest/kotlin/…/FirestorePathParserTest.kt` — path parsing.

Presentation and integration tests are not currently included in the repo.

---

## 10. Key patterns to remember

| Pattern | Where you see it |
|---------|------------------|
| **Repository** | `domain/repository/*` (interfaces), `data/repository/*Impl.kt` |
| **Use case** | `domain/usecase/*` — one class per repo method, invocable via `operator fun invoke` |
| **DTO/Mapper** | `data/dto/FirestoreDtos.kt` + `data/mapper/Mappers.kt` |
| **State machine** | `sealed interface UiState<T>` |
| **One-shot event** | `SharedFlow<XxxEvent>` inside ViewModels |
| **expect / actual** | Wherever the code cannot avoid the platform |
| **Sealed navigation route** | `Routes.kt`, all `@Serializable data class/object` |
| **Composition local** | `LocalIsAdmin`, `LocalAdminUser` in `theme/AdminLocals.kt` |

That is the architecture. Every screen or feature you read about later is built
on top of these ideas.
