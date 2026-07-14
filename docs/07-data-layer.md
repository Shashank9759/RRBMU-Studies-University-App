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

# 07 — Data Layer

This is where the interfaces in `domain/repository/` get real implementations.
Everything Firebase, SQLDelight, Settings, and platform file I/O lives here.

Location: `data/src/commonMain/kotlin/com/studies/rrbmustudies/data/`.

---

## 1. Package layout

```
data/
├── dto/          # @Serializable Firestore DTOs
├── mapper/       # DTO ↔ Domain converters
├── remote/       # Abstract data source interfaces
│   ├── firestore/       # GitLive Firestore implementations
│   └── (StorageRemoteDataSource, RemotePaperRef, FirestorePaths, PathParser)
├── local/        # SQLDelight-backed local data sources
├── repository/   # …RepositoryImpl classes
├── platform/     # PdfVaultFileStore expect declaration
└── di/           # DataModule + PlatformModule expect
```

Plus `data/src/commonMain/sqldelight/…` for the two `.sq` files.

---

## 2. DTOs — `dto/FirestoreDtos.kt`

Each Firestore collection has one DTO. They mirror the domain models but with
`@Serializable`, `String` enums, and no computed properties.

| DTO | Corresponds to |
|-----|----------------|
| `CourseDto` | `Course` |
| `SystemDto` | `CourseSystem` |
| `PartDto` | `Part` |
| `PaperDto` | `Paper` |
| `HomeAdDto` | `HomeAd` |
| `HomeCarouselSettingsDto` | `HomeCarouselSettings` |
| `NotificationDto` | `AppNotification` |
| `FeedbackDto` | `Feedback` |
| `AdminDto` | Firestore `admins/{uid}` doc |

Fields with default values map to Firestore-optional fields. All the enums
(`CourseLevel`, `SystemType`, `NotificationCategory`) become `String`.

---

## 3. Mappers — `mapper/Mappers.kt`

A single file of extension functions:

```kotlin
fun CourseDto.toDomain(id: String): Course
fun Course.toDto(): CourseDto
fun SystemDto.toDomain(id: String, courseId: String): CourseSystem
fun PaperDto.toDomain(id: String, courseId, systemId, partId): Paper
… etc.
```

There are also enum helpers that normalise unknown strings to sensible
defaults (e.g. `"YEARLY"` → `SystemType.YEARLY`, unknown → `YEARLY`).

---

## 4. Remote data sources

All remote sources are abstract Kotlin interfaces in `remote/`. Their
implementations use GitLive's Firestore SDK and live in
`remote/firestore/`.

### 4.1 Content sources — `remote/ContentRemoteDataSource.kt`

| Interface | Purpose |
|-----------|---------|
| `CourseRemoteDataSource` | Observe & save courses, and seed the yearly/semester/entrance skeleton for a new course. |
| `PaperRemoteDataSource` | Observe papers within a part, one paper, recent papers, save/delete a paper, atomically increment `downloadCount`. |
| `SearchRemoteDataSource` | Client-side global search across all papers. |

**Implementation notes** (`remote/firestore/FirestoreContentDataSource.kt`)

- `FirestoreCourseDataSource.observeCourses()` — `collection("courses").orderBy("order").snapshots`.
- `seedCourseSkeleton(courseId)` — writes `systems/yearly`, `systems/semester`,
  `systems/entrance` and generates default `parts/*` based on `durationYears`.
- `FirestorePaperDataSource.observeRecentPapers()` uses
  `firestore.collectionGroup("papers")` to grab papers from anywhere in the
  tree, then sorts client-side by `createdAt DESC`. No composite index needed.
- `incrementDownloadCount()` runs a Firestore transaction.
- `FirestoreSearchDataSource.searchPapers(q, limit)` loads *all* published
  papers (via `collectionGroup`), then filters client-side matching `title`,
  `subject`, `paperCode`, or `description`. Simple; scales up to a few
  thousand papers.

### 4.2 App-config sources — `remote/AppRemoteDataSource.kt`

| Interface | Purpose |
|-----------|---------|
| `HomeAdRemoteDataSource` | Observe active vs all ads, save/delete an ad, observe/save carousel settings. |
| `NotificationRemoteDataSource` | Observe active / all notifications; save/delete. |
| `FeedbackRemoteDataSource` | Write-only: append one feedback doc. |
| `AuthRemoteDataSource` | `currentUid: Flow<String?>`, `signIn`, `signOut`, `isAdmin(uid)`, `getAdminProfile(uid)`. |

Implementations in `remote/firestore/FirestoreAppDataSource.kt`.

- `observeActiveAds()` — `where("isActive", "==", true).orderBy("order")`.
- `observeCarouselSettings()` — reads the single fixed doc
  `app_settings/home_carousel`. Emits default `4 s` if absent.
- `signIn()` calls Firebase Auth, then reads `admins/{uid}`. If the doc is
  missing it **auto-signs-out** and returns `Result.failure` — that is how
  student accounts (that somehow have a Firebase user) are prevented from
  getting admin rights.

### 4.3 Storage source — `remote/StorageRemoteDataSource.kt`

```
suspend fun uploadFile(path: String, bytes: ByteArray, contentType: String): String
```

Implementation: `FirebaseStorageDataSource` in
`remote/firestore/FirebaseStorageDataSource.kt`. Sets the content type in
metadata so the browser can preview PDFs and images instead of downloading
them as `octet-stream`.

Paths written:
- `course_backgrounds/{courseId}.{ext}`
- `papers/{courseId}/{systemId}/{partId}/{paperId}.pdf`
- `ads/{generatedId}.{ext}`
- `notification_attachments/{generatedId}.{ext}`

### 4.4 Path helpers — `remote/FirestorePaths.kt`, `remote/FirestorePathParser.kt`, `remote/RemotePaperRef.kt`

`FirestorePaths` is a set of constants like:
```
courses(): "courses"
paper(cid, sid, pid, pxid): "courses/{cid}/systems/{sid}/parts/{pid}/papers/{pxid}"
```

`FirestorePathParser` extracts `courseId/systemId/partId/paperId` from a
document path — used by the search source and the collection-group queries.

---

## 5. Local data sources

### 5.1 SQLDelight schemas

Under `data/src/commonMain/sqldelight/com/studies/rrbmustudies/data/local/`.

**`CachedPaper.sq`** — recently viewed papers (cap 50):
```sql
CREATE TABLE CachedPaper (
  id TEXT NOT NULL PRIMARY KEY,
  courseId TEXT NOT NULL,
  systemId TEXT NOT NULL,
  partId TEXT NOT NULL,
  title TEXT NOT NULL,
  subject TEXT NOT NULL,
  paperCode TEXT NOT NULL,
  year INTEGER NOT NULL,
  pdfUrl TEXT NOT NULL,
  coverImageUrl TEXT,
  cachedAt INTEGER NOT NULL
);
```

**`DownloadedPaper.sq`** — offline vault:
```sql
CREATE TABLE DownloadedPaper (
  id TEXT NOT NULL PRIMARY KEY,
  courseId TEXT NOT NULL,
  systemId TEXT NOT NULL,
  partId TEXT NOT NULL,
  title TEXT NOT NULL,
  subject TEXT NOT NULL,
  paperCode TEXT NOT NULL,
  year INTEGER NOT NULL,
  remotePdfUrl TEXT NOT NULL,
  localPath TEXT NOT NULL,
  fileSizeBytes INTEGER NOT NULL,
  downloadedAt INTEGER NOT NULL
);
```

Both come with queries for insert-or-replace, list ordered by `cachedAt`/
`downloadedAt` DESC, select by id, delete-by-id, `deleteAll`, `count`, and for
downloads `totalBytes`.

Database class generated by SQLDelight: **`RrbmuDatabase`** in package
`com.studies.rrbmustudies.data.local`.

### 5.2 Local source implementations

- **`SqlDelightPaperLocalDataSource`** — Flow<List<Paper>> of cached papers,
  suspend `cachePaper(paper)`, suspend `trimCache(max)`.
- **`SqlDelightDownloadedPaperLocalDataSource`** — Flow of all downloads,
  Flow of storage info (`count` + `SUM(fileSizeBytes)`), Flow<Boolean> of
  "is this paper downloaded", suspend `upsert`, `deleteById`, `deleteAll`.

Both hold the generated `RrbmuDatabase` instance injected via Koin from
`PlatformModule` (Android uses SharedSQLite driver; iOS uses NativeSQLite).

### 5.3 File store — `platform/PdfVaultFileStore.kt`

An `expect class` with three suspend methods:
```kotlin
expect class PdfVaultFileStore {
    suspend fun writeBytes(paperId: String, bytes: ByteArray, extension: String = "pdf"): String
    suspend fun readBytes(localPath: String): ByteArray
    suspend fun delete(localPath: String)
    suspend fun deleteAll()
    suspend fun exists(localPath: String): Boolean
}
```

`actual` implementations:
- **Android** — writes to `context.filesDir/pdf_vault/{paperId}.pdf`.
- **iOS** — writes to `NSDocumentDirectory/pdf_vault/{paperId}.pdf`.

---

## 6. Repository implementations — `repository/`

Each implementation composes a remote source with a local source (where
relevant) plus mappers, and exposes the domain-level API.

### CourseRepositoryImpl
- `getAllCourses()` — remote flow, **falls back** to `CourseSeed` on error so
  the UI never shows a blank list on a bad network.
- `createCourse()` writes the top-level doc then calls
  `remote.seedCourseSkeleton(courseId)` (creates 3 systems + parts).
- `uploadCourseBackground()` picks the right MIME type (`image/png` / `jpeg`
  / `webp`) then uploads to `course_backgrounds/…`.

### PaperRepositoryImpl
- Wraps the remote paper source.
- `getCachedPapers()` reads from SQLDelight; `cacheViewedPaper()` writes,
  then trims cache to max 50 entries.
- `uploadPaper()` uploads the PDF to Storage first, then writes the Firestore
  doc with the resulting `pdfUrl`.

### SearchRepositoryImpl
- Delegates directly to `FirestoreSearchDataSource` and maps DTOs to domain.

### HomeAdRepositoryImpl
- `getActiveAds()` — remote flow, falls back to `HomeDefaults.promoAds` on
  error. This is why the Home screen never shows an empty carousel.
- `uploadAdImage()` — writes to `ads/{generateId()}.{ext}`.
- `reorderAds()` uses a Firestore batch write.

### NotificationRepositoryImpl
- Straight passthrough. Attachment upload writes to
  `notification_attachments/{id}.{ext}` and returns the URL.

### FeedbackRepositoryImpl
- Generates a UUID id and `createdAt = Clock.System.now().toEpochMilliseconds()`
  before writing.

### PdfVaultRepositoryImpl
- `downloadToVault(paper)`:
  1. Http-GET the paper's `pdfUrl` to bytes (uses platform HTTP — see 09-local-storage.md).
  2. `PdfVaultFileStore.writeBytes(paperId, bytes)` → returns local path.
  3. `DownloadedPaperLocalDataSource.upsert(DownloadedPaper(...))`.
  4. Return `Result.success(downloadedPaper)`.
- `observeStorageInfo()` — Flow of `VaultStorageInfo` combining count + sum bytes.
- `clearVault()` — `PdfVaultFileStore.deleteAll()` + `deleteAll()` on DB.

### SettingsRepositoryImpl
- Wraps `ObservableSettings` from multiplatform-settings.
- Keys: `theme_mode`, `read_notification_ids` (CSV, capped at 300 IDs),
  `paper_open_count`, `last_full_screen_ad_at`, `skip_next_interstitial`.

### AuthRepositoryImpl
- `currentUser` is `authRemote.currentUid` mapped: for each new UID, calls
  `getAdminProfile(uid)` and emits an `AdminUser?`.
- `isAdmin` derives from whether the profile lookup succeeded.

### FirebaseAnalyticsRepository — `repository/FirebaseAnalyticsRepository.kt`
- Calls GitLive `firebaseAnalytics.logEvent("paper_view", …)` etc.
- No persistence — it forwards straight to Firebase.

---

## 7. DI

`di/DataModule.kt` — a Koin module binding every abstract interface to its
concrete impl. Full breakdown in **10-dependency-injection.md**.

`di/PlatformModule.kt` — an `expect val platformModule: Module` declared in
common code. Its `actual` values in Android and iOS provide the SQLDelight
driver, the `ObservableSettings` (SharedPreferences vs NSUserDefaults), and
the `PdfVaultFileStore`.

---

## 8. Which mistakes are already handled

- A course doc missing `level`/`durationYears` still parses (default fallback).
- Firestore going down → the UI still shows built-in defaults for courses & ads.
- A missing admin doc after Firebase Auth login → auto-signs-out.
- A DTO with an unknown enum string → falls back to a sensible default.
- Downloading the same paper twice — the second attempt overwrites the first
  entry (`INSERT OR REPLACE`) and the same file path.

That is the data layer end to end. The next doc drills into the Firebase side
of it (Firestore schema, security rules, functions).
