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

# 25 — Screen: More (Tab 4 of Main)

**File:** `presentation/more/MoreScreen.kt`

The "everything else" tab. No ViewModel — it's a pure UI file with
callback-based navigation.

---

## 1. What it displays

Top to bottom:

1. **`GuestUserCard`** — the top card:
   - If **not** admin: "Guest User — Sign in as admin" style prompt with a
     button that opens `SettingsRoute` (admin login lives in Settings).
   - If **admin**: shows the admin's email + a small `AdminBadge`, and a
     "Go to admin dashboard" button.
2. **"University Services" section** — three `MoreMenuRow`s:
   - University Website → `WebViewRoute("https://…")`
   - Notice Board → `WebViewRoute(...)`
   - Contact University → `WebViewRoute(...)`
   URLs come from `AboutDefaults`.
3. **"App" section** — three `MoreMenuRow`s:
   - **Settings** → `SettingsRoute`
   - **About** → `AboutRoute`
   - **Send Feedback** → `FeedbackRoute`
4. **Ad banner slot** at the bottom.

---

## 2. Parameters

```kotlin
@Composable
fun MoreScreen(
    isAdmin: Boolean,
    onNavigateToFeedback: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenWebView: (url: String, title: String) -> Unit,
    onAdminDashboard: () -> Unit,
    onLoginClick: () -> Unit,
)
```

No state. Every action is a callback into `MainScreen`, which then delegates
to `AppNavHost`.

---

## 3. How admin state is read

`isAdmin` is prop-drilled here, but the same value is available via
`LocalIsAdmin.current`. Both work; the explicit param makes the composable
easier to preview.

---

## 4. External links

Each "University Services" row uses `openWebView(url, title)`. That routes to
`WebViewRoute`, which then opens either:
- Android — a Custom Tab (`androidx.browser:browser`) if the URL is HTTPS.
- iOS — a `WKWebView` embedded inside a `UIKitView`.

See **31-screen-webview.md** for details.

---

## 5. Non-obvious details

- The **"Sign in" prompt for guests routes to Settings, not to a dedicated
  login screen.** This was a UX choice — settings is where admins expect to
  find sign-in-related actions on iOS.
- The Feedback / About / Settings rows are always shown regardless of admin
  status.

Simple screen; no gotchas.
