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

# 11 — Navigation, Routes and Deep Links

The app uses **AndroidX Navigation Compose 2.8** with **type-safe serializable
routes** — no string routes. Everything is in the `navigation/` package inside
`composeApp`.

Files:
- `Routes.kt` — every route data class + `BottomTab` enum
- `AppNavHost.kt` — the `NavHost` that maps each route to a screen
- `AppDeepLinks.kt` — outgoing share URIs + incoming URI parser
- `PdfNavCodec.kt` — Base64-URL-safe encoding for PDF URLs

---

## 1. Routes — `Routes.kt`

Every route is a `@Serializable data object` (no args) or `@Serializable data
class` (with args). The navigation library sees these serialisable classes
and uses them as the destination key.

Full list:

| Route | Args | Destination |
|-------|------|-------------|
| `SplashRoute` | — | Splash |
| `MainRoute` | — | Bottom-tab main screen |
| `CourseDetailRoute` | `courseId, courseName, courseShortName` | Course detail |
| `PartPapersRoute` | `courseId, systemId, partId, partName, courseShortName, systemLabel` | List of papers in a part |
| `PaperDetailRoute` | `courseId, systemId, partId, paperId` | One paper |
| `PdfViewerRoute` | `encodedPdfSource, title` | Full-screen PDF |
| `SearchRoute` | — | Global search |
| `WebViewRoute` | `url, title` | In-app web page |
| `AboutRoute` | — | About / team |
| `LegalDocumentRoute` | `documentId` | Legal doc viewer |
| `FeedbackRoute` | — | Feedback form |
| `SettingsRoute` | — | Settings |
| `DownloadedPapersRoute` | — | Offline vault |
| `NotificationDetailRoute` | `notificationId, openAttachment` | Notification detail |
| `AdminLoginRoute` | — | Admin login |
| `AdminDashboardRoute` | — | Admin dashboard |
| `ManageHomeAdsRoute` | — | Admin: manage home ads |
| `ManageNotificationsAdminRoute` | — | Admin: manage notifications |
| `ManageCoursesAdminRoute` | — | Admin: manage courses |
| `UploadPaperRoute` | `courseId, systemId, partId` (all default empty) | Admin: upload a paper |

Also:

```kotlin
enum class BottomTab { Home, Courses, Notifications, More }
```

`BottomTab` is used *inside* `MainScreen` to switch tabs — it is not a
navigation destination itself.

---

## 2. `AppNavHost.kt` — the navigation host

Simplified structure:

```kotlin
NavHost(navController = navController, startDestination = SplashRoute) {

    composable<SplashRoute>          { SplashScreen(onFinished = { … navigate to MainRoute … }) }
    composable<MainRoute>            { MainScreen(…callbacks that navigate…) }
    composable<CourseDetailRoute>    { entry -> val route = entry.toRoute(); CourseDetailScreen(…) }
    composable<PartPapersRoute>      { entry -> … }
    composable<PaperDetailRoute>     { entry -> … PaperDetailScreen(onOpenPdf = navigate(PdfViewerRoute)) }
    composable<PdfViewerRoute>       { entry -> PdfViewerScreen(pdfUrl = PdfNavCodec.decode(…)) }
    composable<SearchRoute>          { SearchScreen(…) }
    composable<AboutRoute>           { AboutTeamScreen(…) }
    composable<LegalDocumentRoute>   { entry -> LegalDocumentScreen(id) }
    composable<FeedbackRoute>        { FeedbackScreen(…) }
    composable<SettingsRoute>        { SettingsScreen(…) }
    composable<DownloadedPapersRoute>{ DownloadedPapersScreen(…) }
    composable<NotificationDetailRoute>{ entry -> NotificationDetailScreen(…) }
    composable<AdminLoginRoute>      { AdminLoginScreen(onLoginSuccess = navigate(AdminDashboardRoute)) }
    composable<AdminDashboardRoute>  { AdminDashboardScreen(…) }
    composable<ManageCoursesAdminRoute>       { ManageCoursesScreen(…) }
    composable<ManageHomeAdsRoute>            { ManageHomeAdsScreen(…) }
    composable<ManageNotificationsAdminRoute> { ManageNotificationsAdminScreen(…) }
    composable<UploadPaperRoute>     { entry -> UploadPaperScreen(…) }
    composable<WebViewRoute>         { entry -> WebViewScreen(url, title, …) }
}
```

### Notable behaviours

- **Composition locals** for admin state:
  ```kotlin
  CompositionLocalProvider(
      LocalIsAdmin provides isAdmin,
      LocalAdminUser provides adminUser,
  ) { NavHost(…) }
  ```
  Any composable inside can read `LocalIsAdmin.current` to switch its UI.
- The **Splash → Main** transition uses `popUpTo(SplashRoute) { inclusive = true }`
  so the splash never appears in the back stack.
- **`AdminLoginRoute` → `AdminDashboardRoute`** uses the same trick so
  pressing Back from the dashboard goes to wherever the user was before
  login, not back to the login form.
- ViewModels with **route args** are constructed via `parametersOf(…)`, e.g.
  ```kotlin
  koinViewModel<PaperDetailViewModel> {
      parametersOf(route.courseId, route.systemId, route.partId, route.paperId)
  }
  ```

---

## 3. Deep links — `AppDeepLinks.kt`

Two URI hosts are recognised:

```
rrbmustudies://notification/{notificationId}
rrbmustudies://paper/{courseId}/{systemId}/{partId}/{paperId}
```

### 3.1 `AppDeepLinks` object

Helpers for building outgoing links:

- `notification(id)` → `"rrbmustudies://notification/{id}"`.
- `paper(courseId, systemId, partId, paperId)` → paper URI.
- `shareNotification(title, body, id)` → multi-line string used as the payload
  when a user hits "Share" on a notification.
- `sharePaper(title, courseId, systemId, partId, paperId, subject, year)` →
  multi-line string for sharing a paper.

### 3.2 `IncomingDeepLink`
```kotlin
sealed class IncomingDeepLink {
    data class Notification(val notificationId: String, val openAttachment: Boolean = false)
    data class Paper(val courseId: String, val systemId: String, val partId: String, val paperId: String)
}
```

Both `MainActivity` (Android) and the iOS wrapper build one of these from an
incoming URI before calling `App(initialDeepLink = …)`.

### 3.3 `parseAppDeepLink(scheme, host, pathSegments)`

Pure Kotlin. Returns:
- `null` if the scheme is not `rrbmustudies`.
- `IncomingDeepLink.Notification(id)` for `notification/{id}`.
- `IncomingDeepLink.Paper(…)` if there are exactly 4 path segments.

### 3.4 Handling incoming deep links in `AppNavHost`

`AppNavHost` uses a `LaunchedEffect` that fires whenever `initialDeepLink`
changes AND after the splash has completed. It keeps a `handledDeepLinkKey`
so the same link isn't followed twice on a rotation.

If the link arrives *before* the splash finishes, the splash's `onFinished`
lambda opens it. Otherwise `openDeepLink(link, fromSplash = false)` is called.

The behavioural rule: whether you tap a push notification while the app is
cold-started, warm-started, or already foregrounded, you always land on the
same notification detail screen (or paper detail screen).

---

## 4. `PdfNavCodec` — encoding URLs safely

Firebase Storage URLs contain `?token=…&alt=media` — the `?` and `&` break
compose navigation's URL parsing. Solution:

```kotlin
object PdfNavCodec {
    fun encode(urlOrPath: String): String = Base64.UrlSafe.encode(urlOrPath.encodeToByteArray())
    fun decode(encoded: String): String = Base64.UrlSafe.decode(encoded).decodeToString()
}
```

When the user opens a paper's PDF, `PaperDetailScreen` calls
`navController.navigate(PdfViewerRoute(PdfNavCodec.encode(url), title))`.
The viewer decodes it back before loading.

This also lets the same route serve a **local file path** — the downloaded-paper
screen navigates with `PdfNavCodec.encode(localPath)`.

---

## 5. Full navigation graph, ASCII style

```
SplashRoute
   │  onFinished
   ▼
MainRoute ────► BottomTab.Home
             ├─► BottomTab.Courses
             ├─► BottomTab.Notifications
             └─► BottomTab.More
   │
   ├──► SearchRoute
   ├──► SettingsRoute ──► DownloadedPapersRoute
   │                  ├─► ManageHomeAdsRoute
   │                  └─► ManageNotificationsAdminRoute
   ├──► AboutRoute ──► LegalDocumentRoute
   ├──► FeedbackRoute
   ├──► CourseDetailRoute ──► PartPapersRoute ──► PaperDetailRoute ──► PdfViewerRoute
   ├──► NotificationDetailRoute ──► PdfViewerRoute
   ├──► WebViewRoute
   └──► AdminLoginRoute ──► AdminDashboardRoute
                                        ├─► ManageCoursesAdminRoute
                                        ├─► ManageHomeAdsRoute
                                        ├─► ManageNotificationsAdminRoute
                                        └─► UploadPaperRoute
```

That is every possible screen transition in the app.
