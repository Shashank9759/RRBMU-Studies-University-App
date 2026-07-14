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

# 10 — Dependency Injection (Koin)

Every non-trivial class in this app is created by Koin. If you have used
Hilt or Dagger, Koin is a lot lighter — it is service-locator style, no
annotations, no code generation.

---

## 1. Kick-off — `shared/di/KoinInit.kt`

```kotlin
fun initKoin(extraModules: List<Module> = emptyList()) {
    startKoin {
        allowOverride(true)
        modules(listOf(dataModule, domainModule, platformModule) + extraModules)
    }
}
```

**Who calls this?**

- **Android** — `MainActivity.onCreate()` (or an `Application.onCreate()` if
  moved there) calls `initKoin(listOf(androidAdsModule, presentationModule, …))`.
- **iOS** — `MainViewController.kt` calls `initKoin(listOf(presentationModule))`
  before rendering.

`allowOverride(true)` lets platform-specific modules replace a common binding
(this is how iOS could swap `NoOpPaperAdGateway` for something else if a
future iOS ads implementation is added).

---

## 2. Common modules

Four modules are always registered.

### 2.1 `domainModule` — `shared/di/DomainModule.kt`

One `factoryOf(...)` per use case. There are ~47 of them. Sample:

```kotlin
val domainModule = module {
    factoryOf(::GetCoursesUseCase)
    factoryOf(::GetAllCoursesUseCase)
    factoryOf(::GetCourseUseCase)
    factoryOf(::GetCourseSystemsUseCase)
    factoryOf(::GetPartsUseCase)
    factoryOf(::GetPapersUseCase)
    factoryOf(::GetPaperUseCase)
    factoryOf(::GetRecentPapersUseCase)
    factoryOf(::GetCachedPapersUseCase)
    factoryOf(::CacheViewedPaperUseCase)
    factoryOf(::IncrementDownloadCountUseCase)
    factoryOf(::DownloadPaperToVaultUseCase)
    factoryOf(::ObservePaperDownloadedUseCase)
    factoryOf(::ObserveVaultDownloadsUseCase)
    factoryOf(::ObserveVaultStorageUseCase)
    factoryOf(::GetVaultDownloadUseCase)
    factoryOf(::ClearPdfVaultUseCase)
    factoryOf(::SearchPapersUseCase)
    factoryOf(::GetHomeAdsUseCase)
    factoryOf(::GetActiveHomeAdsUseCase)
    factoryOf(::ObserveHomeCarouselSettingsUseCase)
    factoryOf(::GetNotificationsUseCase)
    factoryOf(::GetAllNotificationsUseCase)
    factoryOf(::SubmitFeedbackUseCase)
    factoryOf(::ObserveAdminStateUseCase)
    factoryOf(::SignInAdminUseCase)
    factoryOf(::SignOutAdminUseCase)
    factoryOf(::CreateCourseUseCase)
    factoryOf(::UpdateCourseUseCase)
    factoryOf(::UploadCourseBackgroundUseCase)
    factoryOf(::CreateHomeAdUseCase)
    factoryOf(::UpdateHomeAdUseCase)
    factoryOf(::DeleteHomeAdUseCase)
    factoryOf(::ReorderHomeAdsUseCase)
    factoryOf(::UploadHomeAdImageUseCase)
    factoryOf(::SaveCarouselSettingsUseCase)
    factoryOf(::CreateNotificationUseCase)
    factoryOf(::UpdateNotificationUseCase)
    factoryOf(::DeleteNotificationUseCase)
    factoryOf(::UploadNotificationAttachmentUseCase)
    factoryOf(::UploadPaperUseCase)
    factoryOf(::UpdatePaperUseCase)
    factoryOf(::DeletePaperUseCase)
    factoryOf(::ObserveThemeModeUseCase)
    factoryOf(::SetThemeModeUseCase)
    factoryOf(::LogPaperViewUseCase)
    factoryOf(::LogDownloadUseCase)
    factoryOf(::LogSearchUseCase)
}
```

Use cases are **factories**, not singletons — each ViewModel gets its own
instance (they hold no state so it doesn't matter).

### 2.2 `dataModule` — `data/di/DataModule.kt`

All bindings are `single { … }` — the data layer only wants one instance of
each source and repository per app process.

| Interface | Impl |
|-----------|------|
| `PaperLocalDataSource` | `SqlDelightPaperLocalDataSource` |
| `DownloadedPaperLocalDataSource` | `SqlDelightDownloadedPaperLocalDataSource` |
| `CourseRemoteDataSource` | `FirestoreCourseDataSource` |
| `PaperRemoteDataSource` | `FirestorePaperDataSource` |
| `SearchRemoteDataSource` | `FirestoreSearchDataSource` |
| `HomeAdRemoteDataSource` | `FirestoreHomeAdDataSource` |
| `NotificationRemoteDataSource` | `FirestoreNotificationDataSource` |
| `FeedbackRemoteDataSource` | `FirestoreFeedbackDataSource` |
| `AuthRemoteDataSource` | `FirestoreAuthDataSource` |
| `StorageRemoteDataSource` | `FirebaseStorageDataSource` |
| `CourseRepository` | `CourseRepositoryImpl` |
| `PaperRepository` | `PaperRepositoryImpl` |
| `SearchRepository` | `SearchRepositoryImpl` |
| `HomeAdRepository` | `HomeAdRepositoryImpl` |
| `NotificationRepository` | `NotificationRepositoryImpl` |
| `FeedbackRepository` | `FeedbackRepositoryImpl` |
| `PdfVaultRepository` | `PdfVaultRepositoryImpl` |
| `AuthRepository` | `AuthRepositoryImpl` |
| `SettingsRepository` | `SettingsRepositoryImpl` |
| `AnalyticsRepository` | `FirebaseAnalyticsRepository` |

### 2.3 `platformModule` — `data/di/PlatformModule.kt` (expect/actual)

An `expect val platformModule: Module`.

- **Android** (`data/androidMain/…/PlatformModule.android.kt`)
  - `single<SqlDriver> { AndroidSqliteDriver(RrbmuDatabase.Schema, get(), "RrbmuDatabase.db") }`
  - `single { RrbmuDatabase(get()) }`
  - `single<ObservableSettings> { SharedPreferencesSettings(get<SharedPreferences>()).toObservableSettings() }`
  - `single { PdfVaultFileStore(get()) }` (needs `Context`)
- **iOS** (`data/iosMain/…/PlatformModule.ios.kt`)
  - `single<SqlDriver> { NativeSqliteDriver(RrbmuDatabase.Schema, "RrbmuDatabase.db") }`
  - `single { RrbmuDatabase(get()) }`
  - `single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults).toObservableSettings() }`
  - `single { PdfVaultFileStore() }`

### 2.4 `presentationModule` — `composeApp/di/PresentationModule.kt`

ViewModels. Simple ones use `viewModelOf(::Foo)`. Parameterised ones use
the full `viewModel { (a: String, b: String) -> Foo(get(), a, b) }` form
because their construction depends on route arguments.

```kotlin
val presentationModule = module {
    single<PaperAdGateway> { NoOpPaperAdGateway }   // overridden by Android ads module
    viewModelOf(::AdminViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::CoursesViewModel)
    viewModelOf(::AppSettingsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::DownloadedPapersViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::NotificationsViewModel)
    viewModelOf(::FeedbackViewModel)
    viewModelOf(::AdminLoginViewModel)
    viewModelOf(::ManageHomeAdsViewModel)
    viewModelOf(::ManageCoursesViewModel)
    viewModelOf(::ManageNotificationsViewModel)

    viewModel { (courseId: String, courseName: String) ->
        CourseDetailViewModel(get(), get(), get(), courseId, courseName)
    }
    viewModel { (courseId: String, systemId: String, partId: String, partName: String, isAdmin: Boolean) ->
        PartPapersViewModel(get(), courseId, systemId, partId, partName, isAdmin)
    }
    viewModel { (courseId: String, systemId: String, partId: String, paperId: String) ->
        PaperDetailViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            courseId, systemId, partId, paperId,
        )
    }
    viewModel { (notificationId: String) ->
        NotificationDetailViewModel(notificationId, get(), get())
    }
    viewModel { (courseId: String, systemId: String, partId: String) ->
        UploadPaperViewModel(get(), get(), get(), get(), get(), courseId, systemId, partId)
    }
}
```

`NoOpPaperAdGateway` is registered as the default. On Android, `androidAdsModule`
overrides it with a real `AdMobPaperAdGateway`.

### 2.5 `androidAdsModule` — `composeApp/androidMain/di/AndroidAdsModule.kt`

Only registered on Android. Binds:

- `AndroidAdManager` (singleton) — loads/shows AdMob banners, interstitials
  and rewarded ads.
- `PaperAdGateway` → an AdMob-backed implementation that decides whether to
  play an interstitial/rewarded before opening/downloading a paper.

---

## 3. How ViewModels are obtained in Compose

Inside any composable you write:

```kotlin
val vm: HomeViewModel = koinViewModel()
```

For parameterised ViewModels, pass arguments:

```kotlin
val vm = koinViewModel<PaperDetailViewModel> {
    parametersOf(route.courseId, route.systemId, route.partId, route.paperId)
}
```

That is the pattern you see repeated in `AppNavHost.kt` for every screen that
takes route arguments.

---

## 4. How platform-only bindings reach common code

Common code uses `get<PlatformServices>()` (or similar) as if the binding
existed everywhere. On each platform, `PlatformModule` provides it. This
keeps common code framework-free.

---

## 5. Scope

Everything is at the **application scope**. There are no session, activity or
navigation scopes. The `viewModelOf`/`viewModel` DSL under the hood uses the
`ViewModelStoreOwner` provided by AndroidX Navigation Compose, which keys
ViewModels to the current destination — that is how a `CourseDetailViewModel`
survives configuration changes but is disposed when you `popBackStack()`.

That is the complete DI story.
