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

# 24 — Screen: Notification Detail

**Files:**
- `presentation/notifications/NotificationDetailScreen.kt`
- `presentation/notifications/NotificationsViewModel.kt` (contains `NotificationDetailViewModel`)

Reached by tapping a notification, or via the deep link
`rrbmustudies://notification/{id}`.

---

## 1. Route

```kotlin
@Serializable
data class NotificationDetailRoute(
    val notificationId: String,
    val openAttachment: Boolean = false,
)
```

`openAttachment` is set to `true` when a user taps a push that indicates the
attachment should open directly (rare — currently opt-in from the FCM payload).

---

## 2. What it displays

1. **Top bar** — back button + notification title elided.
2. **Header card** — full title, formatted date, and category badge.
3. **Body** — the notification text in `bodyLarge`.
4. **Attachment section** (if any):
   - Image preview (Coil) OR PDF icon depending on the URL extension.
   - Buttons: **Open** / **Share**.
5. **Share** icon in the top bar sends `AppDeepLinks.shareNotification(title, body, id)`.
6. **Ad banner slot** at the bottom.

If the notification has no attachment, only the header and body are shown.

---

## 3. State — `NotificationDetailViewModel`

```kotlin
class NotificationDetailViewModel(
    private val notificationId: String,
    private val getNotifications: GetNotificationsUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AppNotification?>(null)
    val notification: StateFlow<AppNotification?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.markNotificationRead(notificationId)   // fire-and-forget
            getNotifications().collect { list ->
                _state.value = list.firstOrNull { it.id == notificationId }
            }
        }
    }
}
```

- Marks the notification read **before** rendering (so the badge updates
  immediately for the user, even if the list is still loading).
- Uses the same active-notifications flow as the tab; picks the matching one.

---

## 4. Attachment logic

`AppNotification.resolvedAttachmentUrl` selects the first non-blank of
`attachmentUrl`, `linkUrl`, `pageUrl`, `imageUrl` (in that order).

The screen looks at the extension to decide the UI:

| Extension | Behaviour |
|-----------|-----------|
| `.pdf` | Show PDF icon; Open button routes to `PdfViewerRoute` (through `onOpenPdf` callback). |
| `.jpg/jpeg/png/webp/gif` | Show image preview via Coil; Open button routes to `WebViewRoute` (or opens externally). |
| Everything else | Show link icon; Open button uses `openUrl(url)`. |

---

## 5. Callbacks (from `AppNavHost`)

```kotlin
NotificationDetailScreen(
    notificationId = route.notificationId,
    openAttachment = route.openAttachment,
    onBack = { navController.popBackStack() },
    onOpenPdf = { url, title ->
        navController.navigate(PdfViewerRoute(PdfNavCodec.encode(url), title))
    },
    onViewPaperList = { navController.popBackStack() },
)
```

`onViewPaperList` is a placeholder — reserved for a future case where a
notification points to a specific list of papers.

---

## 6. `openAttachment` auto-open

If the route arrives with `openAttachment = true` (from a push that says
"jump straight to the PDF"), the screen fires `onOpenPdf(url, title)` in a
`LaunchedEffect` after the notification loads.

---

## 7. Non-obvious details

- **Reading marks it read.** Even if the user closes the screen quickly, the
  `markNotificationRead` call happens in `init` — before the UI has finished
  rendering.
- **The same URL can be used both in-app and by a browser** because Firebase
  Storage URLs include a token that lets any client read them.
- **No polling.** The notification's content is streamed from Firestore, so
  if an admin edits the title after the user opened it, the change appears
  without any manual refresh.
