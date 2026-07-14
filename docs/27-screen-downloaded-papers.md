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

# 27 — Screen: Downloaded Papers (the Offline Vault)

**Files:**
- `presentation/settings/DownloadedPapersScreen.kt`
- `presentation/settings/DownloadedPapersViewModel.kt`

Lists every PDF the user has saved for offline reading.

---

## 1. Route

```kotlin
@Serializable
data object DownloadedPapersRoute
```

Reached from Settings ("Downloaded Papers").

---

## 2. What it displays

1. **Top bar** — back button + "Downloaded Papers".
2. **Summary strip** — total count + total size (from `VaultStorageInfo`).
3. **`LazyColumn` of `PaperCard`s** (or a compact variant) — one per
   downloaded paper. Each shows title, subject, code, year, file size, and a
   trailing "delete" icon.
4. **`EmptyState`** when there are no downloads.
5. **Ad banner slot** at the bottom.

---

## 3. State — `DownloadedPapersViewModel`

```kotlin
data class DownloadedPapersUiState(
    val papers: List<DownloadedPaper> = emptyList(),
    val storageInfo: VaultStorageInfo = VaultStorageInfo(),
)
```

Injected use cases:
- `ObserveVaultDownloadsUseCase` — Flow<List<DownloadedPaper>>
- `ObserveVaultStorageUseCase` — Flow<VaultStorageInfo>
- (delete function delegates through the repo; no separate use case in code)

### Actions

- `deleteDownload(id: String)` — calls `pdfVaultRepository.deleteById(id)`
  which removes both the file and the DB row.

---

## 4. Callbacks (from `AppNavHost`)

```kotlin
DownloadedPapersScreen(
    onBack = { navController.popBackStack() },
    onOpenPdf = { localPath, title ->
        navController.navigate(PdfViewerRoute(PdfNavCodec.encode(localPath), title))
    },
)
```

The **exact same** `PdfViewerRoute` is used whether you open a paper from
here (local path) or from `PaperDetailScreen` (remote URL). The viewer
handles both.

---

## 5. Sort order

Latest download first (`downloadedAt DESC` from SQLDelight).

---

## 6. What happens if the underlying file is missing

- The DB row still exists, but `File.exists()` is false.
- Opening will show a loading spinner that never completes (mhiew viewer)
  or a PDFKit error on iOS.
- **Mitigation**: `PdfVaultRepositoryImpl` currently trusts the DB. Adding
  a "verify then repair" step would be a small improvement.

---

## 7. Non-obvious details

- **This screen and the Paper Detail screen share the same offline data
  source** (`DownloadedPaperLocalDataSource`) — deleting from here
  immediately updates the "Downloaded ✓" indicator on Paper Detail if that
  screen is open in the back stack.
- The **file size** on each row is computed at download time and stored in
  the DB, not recomputed — so it stays constant even if the file gets
  corrupted somehow.
