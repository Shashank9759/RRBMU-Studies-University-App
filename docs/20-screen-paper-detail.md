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

# 20 — Screen: Paper Detail

**Files:**
- `presentation/paper/PaperScreens.kt` (`PaperDetailScreen`)
- `presentation/paper/PaperDetailViewModel.kt`

The single most complex screen in the student flow. Everything that has to
happen for a user to read a paper (view, download, cache, analytics, ads)
routes through here.

---

## 1. Route

```kotlin
@Serializable
data class PaperDetailRoute(
    val courseId: String,
    val systemId: String,
    val partId: String,
    val paperId: String,
)
```

---

## 2. What it displays

1. **Top bar** — back button + paper title (elided).
2. **Hero card** — subject-coloured background, paper title, subject, code, year, download count.
3. **Actions row**:
   - Primary button: **View PDF** — opens the PDF viewer.
   - Secondary button: **Download offline** — saves to the vault.
   - **Share** icon — uses `AppDeepLinks.sharePaper(…)` + `shareText`.
4. **Download state indicator** — a linear progress bar + text
   ("Downloading…" / "Downloaded ✓" / "Couldn't download…") depending on
   the ViewModel state.
5. **Ad banner slot** at the bottom.

---

## 3. State — `PaperDetailUi`

```kotlin
data class PaperDetailUi(
    val paper: Paper,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadError: String? = null,
    val localPdfPath: String? = null,
)
```

The screen collects `state: StateFlow<UiState<PaperDetailUi>>` and
`events: SharedFlow<PaperDetailEvent>`.

Events:
```kotlin
sealed interface PaperDetailEvent {
    data class OpenPdf(val pathOrUrl: String, val title: String) : PaperDetailEvent
    data class Snackbar(val message: String) : PaperDetailEvent
}
```

---

## 4. ViewModel — construction and dependencies

Ten use cases + one gateway:

- `GetPaperUseCase`
- `IncrementDownloadCountUseCase`
- `CacheViewedPaperUseCase`
- `LogPaperViewUseCase`
- `LogDownloadUseCase`
- `DownloadPaperToVaultUseCase`
- `ObservePaperDownloadedUseCase`
- `GetVaultDownloadUseCase`
- `ObserveAdminStateUseCase`
- `PaperAdGateway` (from Presentation DI; Android has a real one, iOS is No-Op)

Plus route args: `courseId, systemId, partId, paperId`.

---

## 5. On init — `refresh()`

```
_state = Loading
getPaperUseCase(...).collect { paper ->
  if (paper == null) → Error("Paper not found")
  else {
    isDownloaded = getVaultDownloadUseCase(paper.id) != null
    _state = Success(PaperDetailUi(paper, isDownloaded, localPdfPath))
    cacheViewedPaperUseCase(paper)     // remember for Home > Recently viewed
    logPaperViewUseCase(paper.id, courseId)
  }
}
```

Also, a **separate coroutine** watches `observePaperDownloadedUseCase(paperId)`
so if the user downloads or clears their vault from another screen, the UI
updates instantly.

---

## 6. Opening the PDF — `openPdf()`

```
1. Read current state (must be Success).
2. Fetch isAdmin from ObserveAdminStateUseCase.isAdmin.first().
3. paperAdGateway.requestBeforeOpenPaper(isAdmin) {
     if the ad gate approves,
     emit PaperDetailEvent.OpenPdf(pathOrUrl, title).
   }
   – Admins skip ads entirely.
   – Non-admins may be shown a rewarded ad; on close, the lambda runs.
4. pathOrUrl = localPath if downloaded, else paper.pdfUrl.
```

The screen collects the event and calls `onOpenPdf(url, title)` which
navigates to `PdfViewerRoute(PdfNavCodec.encode(url), title)`.

---

## 7. Downloading to vault — `downloadToVault()`

```
1. Guard: skip if already downloading.
2. paperAdGateway.requestBeforeOfflineDownload(isAdmin) {
     performDownload()
   }
3. performDownload():
   - set isDownloading = true, downloadError = null
   - DownloadPaperToVaultUseCase(paper) → Result<DownloadedPaper>
   - onSuccess:
       - trackDownloadOnce() → incrementDownloadCount + logDownload
       - update UI: isDownloaded = true, localPdfPath = downloaded.localPath
       - emit Snackbar "Saved for offline viewing"
       - emit OpenPdf(localPath) — auto-open after download
   - onFailure:
       - message via toUserMessage
       - update UI: isDownloading = false, downloadError = msg
       - emit Snackbar with the message
```

`trackDownloadOnce()` uses a `_downloadTracked: MutableStateFlow<Boolean>`
so the download counter is only bumped once per session even if the user
re-downloads the same paper.

---

## 8. Callback (from `AppNavHost`)

```kotlin
PaperDetailScreen(
    onBack = { navController.popBackStack() },
    onOpenPdf = { url, title ->
        navController.navigate(PdfViewerRoute(PdfNavCodec.encode(url), title))
    },
    viewModel = koinViewModel { parametersOf(route.courseId, ...) },
)
```

Inside the screen, the events flow is collected and dispatched:
```kotlin
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is PaperDetailEvent.OpenPdf -> onOpenPdf(event.pathOrUrl, event.title)
            is PaperDetailEvent.Snackbar -> snackbarHostState.showSnackbar(event.message)
        }
    }
}
```

---

## 9. Non-obvious details

- **The paper is cached the moment it's viewed** — not just when downloaded.
  This is what fills the Home "Recently viewed" list. See
  `CacheViewedPaperUseCase` → `PaperRepositoryImpl.cacheViewedPaper`.
- **`incrementDownloadCount`** is called on successful download only. It
  writes to Firestore in a transaction.
- **Cross-screen consistency**: pressing back to the part list will show the
  bumped download count instantly, because `observePapers` is a live snapshot.
- The **share text** uses `AppDeepLinks.sharePaper(...)`, which produces a
  multi-line body ending with `rrbmustudies://paper/…`. Users who tap that
  link will land straight on this same screen.

That is the full paper detail flow.
