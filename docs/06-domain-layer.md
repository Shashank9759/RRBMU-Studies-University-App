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

# 06 — Domain Layer

The `domain` module is pure Kotlin. Zero framework code. It defines
**what** the app can do; the `data` module decides **how**.

Location: `domain/src/commonMain/kotlin/com/studies/rrbmustudies/domain/`.

---

## 1. Package layout

```
domain/
├── model/          # Data classes and enums
├── repository/     # Interfaces
└── usecase/        # One class per repo method
```

The module also has one test file: `commonTest/CourseSeedTest.kt` that sanity
checks the bundled 31-course seed list.

---

## 2. Models

All models are plain Kotlin `data class` or `enum class`. No serialization
annotations here — DTOs handle that in the data layer.

### 2.1 Content models — `model/ContentModels.kt`

| Class | Fields | Notes |
|-------|--------|-------|
| `Course` | `id, name, shortName, iconUrl?, backgroundImageUrl?, order, isActive, level, durationYears` | `level` is a `CourseLevel` enum. `durationYears` decides how many parts are generated when seeding a course skeleton. |
| `CourseSystem` | `id, courseId, name, type` | `type` is `SystemType` — YEARLY, SEMESTER, ENTRANCE. |
| `Part` | `id, courseId, systemId, name, order, paperCount, description?` | `paperCount` is denormalised for speed. |
| `Paper` | `id, courseId, systemId, partId, title, subject, paperCode, year, description?, pdfUrl, coverImageUrl?, downloadCount, isPublished, createdAt, updatedAt, createdBy?` | `pdfUrl` is a Firebase Storage download URL. |
| `DownloadedPaper` | `id, courseId, systemId, partId, title, subject, paperCode, year, remotePdfUrl, localPath, fileSizeBytes, downloadedAt` | This mirrors the SQLDelight table. |
| `VaultStorageInfo` | `count, totalBytes` (+ computed `sizeLabel`) | The `sizeLabel` returns `"1.2 MB"`, `"340 KB"`, etc. |

### 2.2 App-configuration models — `model/AppModels.kt`

| Class | Fields | Notes |
|-------|--------|-------|
| `HomeAd` | `id, imageUrl, title, description?, linkUrl, order, isActive, createdAt` | Home banner. |
| `HomeCarouselSettings` | `slideIntervalSeconds = 4` | 2–60 s range enforced by ViewModel. |
| `AppNotification` | `id, title, body, imageUrl?, linkUrl?, pageUrl?, attachmentUrl?, category, isActive, createdAt` | Has `hasAttachment` and `resolvedAttachmentUrl` computed properties — falls back attachment → link → page → image. |
| `Feedback` | `id, rating, comment, tags, deviceInfo?, createdAt` | Rating is 1–5. |
| `AdminUser` | `uid, email, displayName?` | Whoever is logged in. |

### 2.3 Enums — `model/Enums.kt`

- `CourseLevel { UG, PG, DIPLOMA }`
- `SystemType { YEARLY, SEMESTER, ENTRANCE }`

### 2.4 Other enums

- `NotificationCategory { ANNOUNCEMENT, TIME_TABLE, IMPORTANT }` — `model/NotificationCategory.kt`
- `ThemeMode { LIGHT, DARK, SYSTEM }` — `model/ThemeMode.kt`

### 2.5 Seed data — `model/CourseIds.kt`, `model/CourseSeed.kt`, `model/HomeDefaults.kt`

- **`CourseIds`** — 31 course ID string constants (`BSC`, `BCOM`, `MCA`, `MBBS`, `LLB`, …).
- **`CourseSeed`** — data structures used by the Node seed script and as
  fallback content when Firestore fails to load.
- **`HomeDefaults`** — a hardcoded list of promo ads used when Firestore
  returns no active ads.

### 2.6 Result — `model/Result.kt`

A tiny type-alias / helper around `kotlin.Result<T>`. Repository methods that
can fail return this.

---

## 3. Repository interfaces (`repository/`)

There are **10** repository interfaces. Every method returns either a
`Flow<T>` (for continuous data) or a `suspend` function returning
`Result<T>` (for one-shot writes).

### CourseRepository
```
getCourses(level: CourseLevel? = null): Flow<List<Course>>
getAllCourses(): Flow<List<Course>>
getCoursesByLevel(level): Flow<List<Course>>
getCourse(id): Flow<Course?>
getSystems(courseId): Flow<List<CourseSystem>>
getParts(courseId, systemId): Flow<List<Part>>
createCourse(course: Course): Result<Unit>
updateCourse(course: Course): Result<Unit>
uploadCourseBackground(courseId, bytes, fileName): Result<String>
```

### PaperRepository
```
getPapers(courseId, systemId, partId, subject?, year?, isAdmin): Flow<List<Paper>>
getPaper(courseId, systemId, partId, paperId): Flow<Paper?>
getRecentPapers(limit = 10): Flow<List<Paper>>
getCachedPapers(limit = 20): Flow<List<Paper>>
cacheViewedPaper(paper): Result<Unit>
incrementDownloadCount(courseId, systemId, partId, paperId): Result<Unit>
uploadPaper(paper, pdfBytes, pdfFileName): Result<String>   // returns paperId
updatePaper(paper): Result<Unit>
deletePaper(courseId, systemId, partId, paperId): Result<Unit>
```

### SearchRepository
```
searchPapers(query, limit): Flow<List<Paper>>
```

### HomeAdRepository
```
getActiveAds(): Flow<List<HomeAd>>
getAllAds(): Flow<List<HomeAd>>
observeCarouselSettings(): Flow<HomeCarouselSettings>
saveCarouselSettings(settings): Result<Unit>
createAd(ad): Result<Unit>
updateAd(ad): Result<Unit>
reorderAds(orderedIds): Result<Unit>
deleteAd(id): Result<Unit>
uploadAdImage(bytes, fileName): Result<String>
```

### NotificationRepository
```
getNotifications(): Flow<List<AppNotification>>       // isActive == true
getAllNotifications(): Flow<List<AppNotification>>    // admin — includes drafts
createNotification(n): Result<Unit>
updateNotification(n): Result<Unit>
deleteNotification(id): Result<Unit>
uploadNotificationAttachment(bytes, fileName): Result<String>
```

### PdfVaultRepository
```
observeDownloads(): Flow<List<DownloadedPaper>>
observeStorageInfo(): Flow<VaultStorageInfo>
observeIsDownloaded(paperId): Flow<Boolean>
getDownload(paperId): DownloadedPaper?
downloadToVault(paper: Paper): Result<DownloadedPaper>
clearVault(): Result<Unit>
```

### SettingsRepository
```
themeMode: Flow<ThemeMode>
setThemeMode(mode): Unit                          // suspend
readNotificationIds: Flow<Set<String>>
markNotificationRead(id): Unit                    // suspend
getPaperOpenCount(): Int                          // suspend
incrementPaperOpenCount(): Int                    // suspend, returns new value
getLastFullScreenAdAt(): Long
setLastFullScreenAdAt(ts): Unit
getSkipNextInterstitial(): Boolean
setSkipNextInterstitial(v): Unit
```

### AuthRepository
```
currentUser: Flow<AdminUser?>
isAdmin: Flow<Boolean>
signIn(email, password): Result<AdminUser>
signOut(): Result<Unit>
```

### FeedbackRepository
```
submitFeedback(feedback): Result<Unit>
```

### AnalyticsRepository
```
logPaperView(paperId, title)
logDownload(paperId, title)
logSearch(query)
```

---

## 4. Use cases (`usecase/`)

There are ~47 use case classes. Each one wraps exactly one repository method
and is `operator invoke`-able so the ViewModel can call it like a function.

**Design rationale.** Wrapping every repo method in a use case may look
verbose, but it gives you:

1. A stable API on the "downstream" side (the ViewModel).
2. Trivial mocking in tests.
3. A single hook to add cross-cutting concerns (logging, coalescing, throttling).

The use cases are grouped across 4 files.

### `UseCases.kt` — most of them
Course: `GetCoursesUseCase`, `GetAllCoursesUseCase`, `GetCourseUseCase`,
`GetCourseSystemsUseCase`, `GetPartsUseCase`.
Paper: `GetPapersUseCase`, `GetPaperUseCase`, `GetRecentPapersUseCase`,
`GetCachedPapersUseCase`, `CacheViewedPaperUseCase`,
`IncrementDownloadCountUseCase`.
Search: `SearchPapersUseCase`.
Home ads: `GetHomeAdsUseCase`, `GetActiveHomeAdsUseCase`,
`ObserveHomeCarouselSettingsUseCase`.
Notifications: `GetNotificationsUseCase`, `GetAllNotificationsUseCase`.
Vault: `ObserveVaultDownloadsUseCase`, `ObserveVaultStorageUseCase`,
`ObservePaperDownloadedUseCase`, `GetVaultDownloadUseCase`,
`DownloadPaperToVaultUseCase`, `ClearPdfVaultUseCase`.
Feedback: `SubmitFeedbackUseCase`.
Auth: `ObserveAdminStateUseCase` — combines `currentUser` and `isAdmin`.

### `AdminUseCases.kt`
`SignInAdminUseCase`, `SignOutAdminUseCase`, `CreateCourseUseCase`,
`UpdateCourseUseCase`, `UploadCourseBackgroundUseCase`, `CreateHomeAdUseCase`,
`UpdateHomeAdUseCase`, `DeleteHomeAdUseCase`, `ReorderHomeAdsUseCase`,
`UploadHomeAdImageUseCase`, `SaveCarouselSettingsUseCase`,
`CreateNotificationUseCase`, `UpdateNotificationUseCase`,
`DeleteNotificationUseCase`, `UploadNotificationAttachmentUseCase`,
`UploadPaperUseCase`, `UpdatePaperUseCase`, `DeletePaperUseCase`.

### `AnalyticsUseCases.kt`
`LogPaperViewUseCase`, `LogDownloadUseCase`, `LogSearchUseCase`.

### `SettingsUseCases.kt`
`ObserveThemeModeUseCase`, `SetThemeModeUseCase`.

### Example — the pattern

```kotlin
class GetCoursesUseCase(private val repo: CourseRepository) {
    operator fun invoke(level: CourseLevel? = null): Flow<List<Course>> =
        repo.getCourses(level)
}

class SignInAdminUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<AdminUser> =
        repo.signIn(email, password)
}
```

---

## 5. What is NOT in the domain layer

- No Firebase types.
- No Compose types.
- No Android or iOS imports.
- No JSON annotations.
- No SQL.
- No logging framework (uses `println` in extremely rare places if anywhere).

That is the whole domain layer. Everything the app does at a business level
is described here, and every other module ultimately calls into these use cases.
