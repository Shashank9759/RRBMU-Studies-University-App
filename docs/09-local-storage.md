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

# 09 — Local Storage (SQLDelight + Settings + File Store)

Every piece of data that survives across app restarts but does not live in
Firebase is stored on the device with one of three mechanisms.

| Mechanism | Purpose | Files |
|-----------|---------|-------|
| **SQLDelight (RrbmuDatabase)** | Structured data: recently viewed papers cache, downloaded paper metadata | `data/src/commonMain/sqldelight/…` |
| **multiplatform-settings** | Small key-value preferences: theme, ad throttling counters, notification read IDs | `data/src/commonMain/.../SettingsRepositoryImpl.kt` |
| **PdfVaultFileStore** | The actual PDF bytes | `data/src/commonMain/.../platform/PdfVaultFileStore.kt` (`expect`) + android/ios `actual` |

Below, each one in detail.

---

## 1. SQLDelight database — `RrbmuDatabase`

### 1.1 Where it lives on disk

- **Android** — SQLite file `RrbmuDatabase.db` in the app's `databases/`
  directory (managed by `AndroidSqliteDriver`).
- **iOS** — SQLite file `RrbmuDatabase.db` in the app's Documents directory
  (managed by `NativeSqliteDriver`).

### 1.2 Schema

Two tables, both defined by `.sq` files that SQLDelight compiles into typed
Kotlin queries.

#### `CachedPaper.sq`
Stores the most recently viewed papers so the Home screen can show a
"recently viewed" section without hitting Firestore. Capped at ~50 rows.

```
CREATE TABLE CachedPaper (
    id            TEXT NOT NULL PRIMARY KEY,
    courseId      TEXT NOT NULL,
    systemId      TEXT NOT NULL,
    partId        TEXT NOT NULL,
    title         TEXT NOT NULL,
    subject       TEXT NOT NULL,
    paperCode     TEXT NOT NULL,
    year          INTEGER NOT NULL,
    pdfUrl        TEXT NOT NULL,
    coverImageUrl TEXT,
    cachedAt      INTEGER NOT NULL
);
```

Queries: `insertOrReplace`, `selectAllOrdered` (DESC by `cachedAt`), `selectById`,
`deleteOlderThanNth` (trim to 50), `count`.

#### `DownloadedPaper.sq`
Stores metadata about PDFs saved to the offline vault.

```
CREATE TABLE DownloadedPaper (
    id            TEXT NOT NULL PRIMARY KEY,
    courseId      TEXT NOT NULL,
    systemId      TEXT NOT NULL,
    partId        TEXT NOT NULL,
    title         TEXT NOT NULL,
    subject       TEXT NOT NULL,
    paperCode     TEXT NOT NULL,
    year          INTEGER NOT NULL,
    remotePdfUrl  TEXT NOT NULL,
    localPath     TEXT NOT NULL,
    fileSizeBytes INTEGER NOT NULL,
    downloadedAt  INTEGER NOT NULL
);
```

Queries: `insertOrReplace`, `selectAllOrdered` (DESC by `downloadedAt`),
`selectById`, `deleteById`, `deleteAll`, `count`, `totalBytes`
(`SELECT SUM(fileSizeBytes) …`).

### 1.3 Generated code

SQLDelight's Gradle plugin generates a `RrbmuDatabase` class in package
`com.studies.rrbmustudies.data.local` with:

- `RrbmuDatabase.Companion.Schema` — schema version metadata.
- `cachedPaperQueries` — one property per query in `CachedPaper.sq`.
- `downloadedPaperQueries` — one property per query in `DownloadedPaper.sq`.

### 1.4 Local data source classes

Both in `data/src/commonMain/.../local/`.

- `SqlDelightPaperLocalDataSource` — Flow<List<Paper>> of cached papers,
  suspend `cachePaper(paper)` writing on the IO dispatcher, and suspend
  `trimCache(max)` to prune old rows.
- `SqlDelightDownloadedPaperLocalDataSource` —
  - `observeAll(): Flow<List<DownloadedPaper>>`
  - `observeStorageInfo(): Flow<VaultStorageInfo>` — combines count + total bytes
  - `observeById(id): Flow<DownloadedPaper?>`
  - `getById(id): DownloadedPaper?` (blocking)
  - `upsert(d: DownloadedPaper)`, `deleteById(id)`, `deleteAll()` — all suspend

Both use `sqldelight-coroutines-extensions` to turn queries into Flows.

### 1.5 Driver injection

The driver is provided per platform through `PlatformModule`.

Android (`data/androidMain/.../di/PlatformModule.android.kt`):
```kotlin
single<SqlDriver> {
    AndroidSqliteDriver(RrbmuDatabase.Schema, get(), "RrbmuDatabase.db")
}
single { RrbmuDatabase(get()) }
```

iOS (`data/iosMain/.../di/PlatformModule.ios.kt`):
```kotlin
single<SqlDriver> {
    NativeSqliteDriver(RrbmuDatabase.Schema, "RrbmuDatabase.db")
}
single { RrbmuDatabase(get()) }
```

---

## 2. multiplatform-settings — `ObservableSettings`

### 2.1 Backing stores

- **Android** — `SharedPreferences` with name `rrbmustudies_prefs`.
- **iOS** — `NSUserDefaults(suiteName: nil)` (i.e. standard user defaults).

Both are wrapped as an `ObservableSettings` (from Russhwolf's library) which
gives Flow updates.

### 2.2 Keys used

| Key | Type | Meaning |
|-----|------|---------|
| `theme_mode` | String (`"LIGHT" \| "DARK" \| "SYSTEM"`) | Which colour scheme the app should use |
| `read_notification_ids` | CSV of IDs, max 300 | Which notifications the user has opened |
| `paper_open_count` | Int | Total papers opened (used by ad throttler) |
| `last_full_screen_ad_at` | Long (epoch ms) | When the last interstitial ran |
| `skip_next_interstitial` | Bool | If a rewarded ad just fired, skip the next interstitial |

### 2.3 Access

Everything goes through `SettingsRepositoryImpl` in
`data/repository/SettingsRepositoryImpl.kt`. It exposes only the higher-level
methods listed in **06-domain-layer.md** (like `themeMode: Flow<ThemeMode>`
and `markNotificationRead(id)`).

---

## 3. PDF vault — file bytes

Metadata lives in SQLDelight. The **actual PDF bytes** live in a private
directory managed by `PdfVaultFileStore`.

### 3.1 Common `expect` declaration

`data/src/commonMain/.../platform/PdfVaultFileStore.kt`:

```kotlin
expect class PdfVaultFileStore {
    suspend fun writeBytes(paperId: String, bytes: ByteArray, extension: String = "pdf"): String
    suspend fun readBytes(localPath: String): ByteArray
    suspend fun delete(localPath: String)
    suspend fun deleteAll()
    suspend fun exists(localPath: String): Boolean
}
```

Returns the local path as a `String` (e.g. `/data/data/…/files/pdf_vault/xyz.pdf`).

### 3.2 Android `actual`
`data/src/androidMain/.../PdfVaultFileStore.android.kt` — writes to
`context.filesDir/pdf_vault/{paperId}.{ext}`. Uses `File.writeBytes` under
`Dispatchers.IO`.

### 3.3 iOS `actual`
`data/src/iosMain/.../PdfVaultFileStore.ios.kt` — writes to
`NSDocumentDirectory/pdf_vault/{paperId}.{ext}` using `NSFileManager`.

### 3.4 Why not just Firebase Storage always?

Because students may be on flaky mobile networks. The vault gives them
**guaranteed offline access** to previously downloaded papers, plus
zero-network open latency.

---

## 4. Flow of a paper being downloaded to the vault

Written out step by step so the moving parts are clear.

1. User taps **Download offline** on `PaperDetailScreen`.
2. `PaperDetailViewModel.downloadToVault()` sets `isDownloading = true`.
3. Ad gateway may play a rewarded ad first (Android only).
4. `DownloadPaperToVaultUseCase(paper)` → `PdfVaultRepositoryImpl.downloadToVault(paper)`.
5. Fetch bytes from `paper.pdfUrl` (Firebase Storage URL) via the platform's
   HTTP client (Android — OkHttp inside Coil is not used here; direct
   HttpURLConnection or Ktor is used — see the concrete impl).
6. `PdfVaultFileStore.writeBytes(paper.id, bytes)` → returns
   `filesDir/pdf_vault/xyz.pdf`.
7. `SqlDelightDownloadedPaperLocalDataSource.upsert(DownloadedPaper(...))`.
8. `observeIsDownloaded(paperId)` emits `true` → UI switches the download
   button to "Open".
9. If the user taps Open, `PaperDetailViewModel.openPdf()` emits
   `PaperDetailEvent.OpenPdf(localPath, title)`, which routes to
   `PdfViewerScreen` with the file-system path.

---

## 5. Clearing the vault

Settings screen → "Clear downloaded papers" fires
`ClearPdfVaultUseCase()` → `PdfVaultRepositoryImpl.clearVault()`, which does:

1. `PdfVaultFileStore.deleteAll()` — removes the `pdf_vault/` folder.
2. `SqlDelightDownloadedPaperLocalDataSource.deleteAll()` — clears the table.

The Vault storage info Flow emits `VaultStorageInfo(count = 0, totalBytes = 0)`
and the UI updates automatically.
