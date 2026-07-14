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

# 37 — End-to-End Data Flows

Textual sequence diagrams of the flows that touch multiple layers. Once you
have read the layer docs (06–10), these show how the pieces snap together.

---

## Flow 1 — Cold start

```
User taps launcher icon
    │
    ▼
Android: MainActivity.onCreate
    │  FirebaseApp.initializeApp(this)
    │  MobileAds.initialize(this)
    │  incomingDeepLink = buildDeepLinkFromIntent(intent)
    │  setContent { App(incomingDeepLink) }
    │
    ▼
App { RrbmuTheme { AppNavHost(startDestination = SplashRoute) } }
    │
    ▼
SplashScreen → LaunchedEffect delay 800ms → onFinished()
    │
    ▼
AppNavHost.openDeepLink(...)  OR  navigate(MainRoute)
    │
    ▼
MainScreen renders Home tab (via HomeViewModel).
```

Behind the scenes, `AdminViewModel` starts collecting `AuthRepository.isAdmin`
so by the time Home renders, `LocalIsAdmin` is already correct.

---

## Flow 2 — Browsing to a paper

```
User taps course tile on Home
    │
    ▼
HomeScreen.onCourseClick(course)
    │
    ▼
navController.navigate(CourseDetailRoute(course.id, course.name, course.shortName))
    │
    ▼
CourseDetailScreen collects CourseDetailViewModel.state
    │
    ▼
CourseDetailViewModel loads course, systems, parts via 3 use cases
    │
    ▼
User taps a Part
    │
    ▼
navigate(PartPapersRoute(...))
    │
    ▼
PartPapersScreen collects PartPapersViewModel.state
    │
    ▼
GetPapersUseCase(courseId, systemId, partId, isAdmin) → Flow<List<Paper>>
    │      → PaperRepositoryImpl → FirestorePaperDataSource.observePapers
    │      → live snapshot
    ▼
User taps a Paper
    │
    ▼
navigate(PaperDetailRoute(courseId, systemId, partId, paperId))
    │
    ▼
PaperDetailScreen (see Flow 3).
```

---

## Flow 3 — Opening a paper's PDF

```
PaperDetailScreen renders, user taps "View PDF"
    │
    ▼
PaperDetailViewModel.openPdf()
    │
    ├─ isAdmin = observeAdminStateUseCase.isAdmin.first()
    ├─ paperAdGateway.requestBeforeOpenPaper(isAdmin) { proceed }
    │       │
    │       ├─ if admin → proceed()
    │       ├─ else if throttled → proceed()
    │       └─ else load & show interstitial; onClose → proceed()
    │
    ▼
emitOpenPdf(current):
    ├─ local = getVaultDownloadUseCase(paper.id)
    ├─ path = local?.localPath ?: paper.pdfUrl
    └─ _events.emit(PaperDetailEvent.OpenPdf(path, title))
    │
    ▼
PaperDetailScreen collects the event → calls onOpenPdf(url, title)
    │
    ▼
AppNavHost: navigate(PdfViewerRoute(PdfNavCodec.encode(path), title))
    │
    ▼
PdfViewerScreen(pdfUrl = PdfNavCodec.decode(...))
    │
    ▼
PlatformPdfViewer(source)
    ├─ Android: mhiew PDFView (streaming download if URL)
    └─ iOS: PDFKit.PDFView
```

---

## Flow 4 — Downloading a paper for offline

```
User taps "Download offline" on PaperDetailScreen
    │
    ▼
PaperDetailViewModel.downloadToVault()
    │
    ├─ isAdmin from ObserveAdminStateUseCase
    ├─ paperAdGateway.requestBeforeOfflineDownload(isAdmin) { performDownload() }
    │
    ▼
performDownload():
    │   set isDownloading = true
    │
    ├─ DownloadPaperToVaultUseCase(paper)
    │       │
    │       ▼
    │   PdfVaultRepositoryImpl.downloadToVault(paper)
    │       ├─ HTTP GET paper.pdfUrl → ByteArray
    │       ├─ PdfVaultFileStore.writeBytes(paper.id, bytes)   ← platform-specific
    │       │       → returns localPath
    │       └─ DownloadedPaperLocalDataSource.upsert(DownloadedPaper(...))
    │           → SQLDelight INSERT OR REPLACE
    │       → Result.success(downloaded)
    │
    ├─ Track once:
    │   ├─ IncrementDownloadCountUseCase  → Firestore txn
    │   └─ LogDownloadUseCase → Firebase Analytics
    │
    ├─ Update UI: isDownloading = false, isDownloaded = true, localPdfPath = ...
    ├─ Snackbar "Saved for offline viewing"
    └─ Emit OpenPdf(localPath) → auto-open in PdfViewerScreen
```

`observeIsDownloaded(paperId)` also fires because the DB row was inserted,
so anywhere else in the app that observes it (e.g. `DownloadedPapersScreen`
in the back stack) updates instantly.

---

## Flow 5 — Admin publishes a notification (student receives push)

```
Admin fills the form on ManageNotificationsAdminScreen
    │
    ▼
ManageNotificationsViewModel.saveNotification(dto)
    │
    ├─ (Optional) UploadNotificationAttachmentUseCase → Storage → URL
    ├─ CreateNotificationUseCase(notification) → Firestore write to notifications/{id}
    │
    ▼
Firebase Cloud Function `onNotificationWrite` triggers
    │  if isActive == true and payload changed
    │
    ▼
admin.messaging().send({ topic: "all_users", notification: {...}, data: {...} })
    │
    ▼
FCM → device
    │
    ├── Android: RrbmuMessagingService.onMessageReceived
    │       → posts NotificationCompat with tap intent that carries
    │         "deep_link_notification_id"
    │
    └── iOS: OS shows banner; on tap AppDelegate parses payload
    │
    ▼
User taps the push
    │
    ▼
MainActivity relaunched with extras (or iOS with UNNotificationResponse)
    │
    ▼
buildDeepLinkFromIntent → IncomingDeepLink.Notification(id, openAttachment)
    │
    ▼
App(initialDeepLink) → AppNavHost.openDeepLink →
navigate(NotificationDetailRoute(id, openAttachment))
    │
    ▼
NotificationDetailViewModel(init) marks read, fetches notification,
if openAttachment == true → onOpenPdf(url, title) → PdfViewerRoute.
```

Meanwhile, `NotificationsViewModel` (still alive in Main) sees the new
notification via its Firestore snapshot flow, and `hasUnread` becomes
`true` momentarily until the detail view marks it read.

---

## Flow 6 — Admin signs in via Settings

```
User (guest) taps "Sign in as Admin" on SettingsScreen
    │
    ▼
SettingsViewModel.showAdminLoginSheet() → showAdminLoginSheet = true
    │
    ▼
AdminLoginBottomSheet rendered with AdminLoginViewModel
    │
    ├─ user types email + password → onEmailChange / onPasswordChange
    ├─ user taps Sign In → signIn()
    │       │
    │       ▼
    │   SignInAdminUseCase(email, password) →
    │   AuthRepositoryImpl.signIn → FirestoreAuthDataSource.signIn:
    │       1. Firebase.auth.signInWithEmailAndPassword
    │       2. Firestore admins/{uid}.get()
    │       3. If missing → auto signOut → Result.failure
    │       4. Otherwise Result.success(AdminUser)
    │
    ▼
AdminLoginViewModel: isSuccess = true
    │
    ▼
Bottom sheet dismissed
    │
    ▼
AuthRepository.isAdmin flow now emits true
    │
    ├── SettingsViewModel state updates: isAdmin = true, adminUser = ...
    ├── AdminViewModel state updates → LocalIsAdmin.current = true across app
    └── Every screen re-renders where LocalIsAdmin is used
```

Now the user sees admin tiles in Settings, admin FABs on Course/Part, and
the admin dashboard entry point on More.

That is the whole app in flow diagrams.
