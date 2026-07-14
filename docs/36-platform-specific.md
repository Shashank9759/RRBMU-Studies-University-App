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

# 36 — Platform-Specific Code (`expect` / `actual`)

Kotlin Multiplatform lets us share ~95 % of the code between Android and iOS.
The remaining 5 % is behind `expect` / `actual` declarations. This file lists
every one of them.

---

## 1. Location convention

- Common `expect`s live under `composeApp/src/commonMain/…/platform/` and
  `data/src/commonMain/…/platform/`.
- Android `actual`s live under `androidMain/…/platform/…android.kt`.
- iOS `actual`s live under `iosMain/…/platform/…ios.kt`.

Same pattern for admin pickers (under `presentation/admin/`).

---

## 2. Common utilities — `composeApp/commonMain/platform/PlatformServices.kt`

```kotlin
expect fun openUrl(url: String)
expect fun shareText(text: String)
```

### Android — `PlatformServices.android.kt`
- `openUrl` uses **Custom Tabs** for `https://` URLs; falls back to
  `Intent.ACTION_VIEW` for `mailto:`, `tel:`, `whatsapp:`, etc.
- `shareText` uses `Intent.ACTION_SEND` wrapped in `Intent.createChooser`.

### iOS — `PlatformServices.ios.kt`
- `openUrl` uses `UIApplication.sharedApplication.openURL(...)`.
- `shareText` uses `UIActivityViewController`.

---

## 3. Notification permission — `composeApp/commonMain/platform/NotificationPermission.kt`

```kotlin
@Composable expect fun RequestNotificationPermission()
```

Called once from `App.kt`.

### Android — `NotificationPermission.android.kt`
On API 33+ requests `POST_NOTIFICATIONS` via the Activity Result API. On
older APIs it is a no-op. Only requests once per install (uses a shared
preference flag to avoid re-prompting).

### iOS — `NotificationPermission.ios.kt`
Calls `UNUserNotificationCenter.currentNotificationCenter.requestAuthorizationWithOptions(...)`
with `.alert | .badge | .sound`.

---

## 4. PDF viewer — `composeApp/commonMain/presentation/paper/PlatformPdfViewer.kt`

```kotlin
@Composable expect fun PlatformPdfViewer(source: String, modifier: Modifier = Modifier)
```

Covered in **21-screen-pdf-viewer.md**.

- Android — `com.github.mhiew:android-pdf-viewer` inside `AndroidView`.
- iOS — `PDFKit.PDFView` inside `UIKitView`.

---

## 5. Ads — `composeApp/commonMain/ads/Ads.kt`

```kotlin
@Composable expect fun AdMobBanner(modifier: Modifier = Modifier)
```

- Android — real `AdView`.
- iOS — no-op (`Spacer(Modifier.height(0.dp))`).

Also see 33-ads-admob.md for the `PaperAdGateway` interface.

---

## 6. Activity holder (Android only) — `composeApp/androidMain/platform/ActivityHolder.kt`

Not an `expect`/`actual` — Android-only. Holds a weak reference to the
current `Activity` so that non-composable code (like `AndroidAdManager`)
can access it. `MainActivity` writes itself in on `onResume`.

---

## 7. Admin pickers — `composeApp/commonMain/presentation/admin/…`

Three composable-returning `expect`s:

```kotlin
@Composable expect fun rememberImagePicker(onPicked: (bytes: ByteArray, name: String) -> Unit): () -> Unit
@Composable expect fun rememberPdfPicker(onPicked: (bytes: ByteArray, name: String) -> Unit): () -> Unit
@Composable expect fun rememberNotificationAttachmentPicker(onPicked: (bytes: ByteArray, name: String) -> Unit): () -> Unit
```

### Android impls (`androidMain/presentation/admin/`)
Use `ActivityResultContracts.OpenDocument` with the right MIME filter. The
returned launcher-closure is passed a filename via
`documentFile.name ?: "file.ext"`.

### iOS impls (`iosMain/presentation/admin/`)
- Image → `PHPickerViewController` from PhotosUI framework.
- PDF / notification attachment → `UIDocumentPickerViewController` with
  `UTType.pdf` (or PDF + image types for the notification variant).

---

## 8. Data-layer platform code

Under `data/src/androidMain/…` and `data/src/iosMain/…`.

### File store — `PdfVaultFileStore`
Already covered in **09-local-storage.md**.

### Firebase init
- Android — `FirebaseInit.android.kt` — `FirebaseApp.initializeApp(context)`
  is called by the Google Services plugin at app startup automatically,
  but the helper is here for completeness.
- iOS — `FirebaseInit.ios.kt` — calls `FIRApp.configure()` explicitly from
  `MainViewController` before Koin starts.

### Platform utils
- `PlatformUtils.android.kt` and `PlatformUtils.ios.kt` — small helpers
  (`generateId()` using `UUID.randomUUID()` on Android, `NSUUID` on iOS;
  epoch time helpers).

### DI Platform module
Already covered in **10-dependency-injection.md**.

---

## 9. iOS entry point — `MainViewController.kt`

`iosMain/kotlin/.../MainViewController.kt`:

```kotlin
fun MainViewController(): UIViewController {
    // Ensure Firebase, Koin, ads gateway are ready.
    initKoin(listOf(presentationModule))
    return ComposeUIViewController { App(initialDeepLink = null) }
}
```

The Swift side calls this from its `SceneDelegate`.

---

## 10. Android entry point — `MainActivity.kt`

Simplified:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this)
        val incoming = buildDeepLinkFromIntent(intent)
        setContent { App(initialDeepLink = incoming) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        // AppNavHost handles the deep-link via LaunchedEffect on initialDeepLink change.
    }

    override fun onResume() { super.onResume(); ActivityHolder.set(this) }
    override fun onPause()  { super.onPause();  ActivityHolder.clear(this) }
}
```

Deep-link parsing bridges FCM taps + share URIs into `IncomingDeepLink`.

That covers every platform-specific piece.
