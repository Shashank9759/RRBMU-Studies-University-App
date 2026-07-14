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

# 21 — Screen: PDF Viewer

**File:** `presentation/paper/PaperScreens.kt` (`PdfViewerScreen`)

Full-screen PDF display. Used for both **remote URLs** (Firebase Storage
signed URLs) and **local file paths** (from the downloaded-paper vault).

---

## 1. Route

```kotlin
@Serializable
data class PdfViewerRoute(
    val encodedPdfSource: String,   // Base64-URL-safe encoded URL or local path
    val title: String = "",
)
```

The encoded source is decoded with `PdfNavCodec.decode(...)` inside the
composable. The reason for encoding is that Firebase Storage download URLs
contain `?token=…&alt=media`, which breaks Compose Navigation's URL parsing.

---

## 2. What it displays

- **Top bar** — back button, title (from route args, elided to one line).
- **Full-screen PDF area** — driven by a platform-specific `expect` composable.

---

## 3. Platform-specific viewer

```kotlin
expect @Composable fun PlatformPdfViewer(
    source: String,
    modifier: Modifier = Modifier,
)
```

### Android — `PlatformPdfViewer.android.kt`
- Wraps `com.github.mhiew:android-pdf-viewer` inside a Compose `AndroidView`.
- Detects whether `source` is a local file path (`/data/…/pdf_vault/…`) or
  an https URL. For URLs, it uses OkHttp to stream to a temporary cache.
- Configures the widget for pinch-zoom, page snap, spacing between pages.
- Shows a progress indicator while the PDF is loading.

### iOS — `PlatformPdfViewer.ios.kt`
- Wraps `PDFKit.PDFView` inside a `UIKitView`.
- Handles local paths (`NSURL(fileURLWithPath:)`) and remote URLs
  (`NSURL(string:)`) equivalently — PDFKit downloads remote ones itself.
- Enables autoscale and horizontal scrolling.

---

## 4. Composable

```kotlin
@Composable
fun PdfViewerScreen(
    pdfUrl: String,
    title: String,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = { RrbmuTopBar(title = title, onBack = onBack) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            PlatformPdfViewer(source = pdfUrl, modifier = Modifier.fillMaxSize())
        }
    }
}
```

`pdfUrl` here is already decoded (by whoever routed here — usually
`AppNavHost.kt`).

---

## 5. Callback

Only one — `onBack()` → `navController.popBackStack()`.

---

## 6. Two invocation paths

1. **From `PaperDetailScreen`** — passes either the remote HTTPS URL or the
   local vault path.
2. **From `DownloadedPapersScreen`** — always passes a local vault path.

Both use the same route and same `PdfNavCodec.encode(...)` step.

---

## 7. Non-obvious details

- **No caching layer** — the viewer relies on the OS-level HTTP cache and on
  the vault for offline files. There is no in-app PDF cache.
- **No annotation / mark-up support** — this is purely a read-only viewer.
- **On iOS the title may look slightly different** because PDFKit adds its
  own header when the PDF has embedded metadata; Android's viewer does not.

That is the entire PDF viewer.
