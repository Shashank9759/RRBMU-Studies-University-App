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

# 15 — Screen: Main (Bottom Nav Scaffold)

**File:** `composeApp/src/commonMain/kotlin/com/studies/rrbmustudies/presentation/main/MainScreen.kt`

`MainScreen` is not a page in itself. It is the **scaffold** that hosts the
four bottom-tab pages: Home, Courses, Notifications, More.

---

## 1. What it displays

A Material 3 `Scaffold` with three parts:

```
┌──────────────────────────────┐
│         RrbmuTopBar          │ ← logo on Home; bell always visible
├──────────────────────────────┤
│                              │
│    (one of the 4 tabs)       │
│                              │
├──────────────────────────────┤
│        RrbmuBottomBar        │ ← Home / Courses / Notif / More
└──────────────────────────────┘
```

The top bar's logo only shows on the Home tab; on the other three tabs it's
hidden. The bell icon (top-right) is always there — tapping it switches to the
Notifications tab.

---

## 2. Parameters

```kotlin
@Composable
fun MainScreen(
    isAdmin: Boolean,
    onCourseClick: (Course) -> Unit,
    onPaperClick: (Paper) -> Unit,
    onSearchClick: () -> Unit,
    onAdClick: (HomeAd) -> Unit,
    onAbout: () -> Unit,
    onFeedback: () -> Unit,
    onSettings: () -> Unit,
    onAdminDashboard: () -> Unit,
    onNotificationDetail: (String) -> Unit,
    onWebLink: (String, String) -> Unit,
    initialTab: BottomTab = BottomTab.Home,
)
```

Every callback is provided by `AppNavHost` — this screen doesn't know how to
navigate anywhere, it just calls the lambdas.

---

## 3. State

- `selectedTab: BottomTab` — held in `rememberSaveable` so switching tabs
  survives process death / rotation.
- `notificationsViewModel: NotificationsViewModel` — obtained once via
  `koinViewModel()`. **Important**: the same instance is passed into the
  Notifications tab, so the tab body and the top-bar badge share state.
- `hasUnread: Boolean` — collected from `notificationsViewModel.hasUnread`.
  Drives the red dot on the bell icon and on the Notifications bottom tab.

---

## 4. Tab dispatch

```kotlin
when (selectedTab) {
    BottomTab.Home          -> HomeScreen(...)
    BottomTab.Courses       -> CoursesScreen(onCourseClick = onCourseClick)
    BottomTab.Notifications -> NotificationsScreen(viewModel = notificationsViewModel, ...)
    BottomTab.More          -> MoreScreen(...)
}
```

Each branch is a full-screen composable. There is no shared state between
tabs beyond the `NotificationsViewModel`. When you switch away, the previous
tab's UI is torn down; when you switch back, its ViewModel is still alive
because it's Koin-scoped (single).

---

## 5. Why is `NotificationsViewModel` created here, not inside `NotificationsScreen`?

Two reasons:

1. The **red dot** on the top bar and the bottom-nav badge need
   `hasUnread` before the user has ever opened the tab. If the ViewModel
   was scoped to `NotificationsScreen`, its Flow wouldn't start emitting
   until the user tapped the tab.
2. Sharing the same instance means the "read" state persists across tab
   switches without extra plumbing — tapping a notification marks it read,
   and both the tab list and the bell badge update instantly.

---

## 6. Data flow into `MainScreen`

```
AppNavHost
  │
  ▼
MainScreen(isAdmin, callbacks…)
  │
  ├─ HomeScreen             — collects HomeViewModel
  ├─ CoursesScreen          — collects CoursesViewModel
  ├─ NotificationsScreen    — passed the shared NotificationsViewModel
  └─ MoreScreen             — pure UI, uses LocalIsAdmin
```

`isAdmin` is prop-drilled from `AppNavHost`, which reads it from
`AdminViewModel`. Inside components, `LocalIsAdmin.current` gives the same
value.

Next up: the four tabs themselves (Home, Courses, Notifications, More).
