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

# 31 — Screen: WebView

**File:** `presentation/more/WebViewScreen.kt`

A generic in-app browser for external HTTPS URLs — used by the More tab's
"University Website" / "Notice Board" / "Contact University" rows.

---

## 1. Route

```kotlin
@Serializable
data class WebViewRoute(
    val url: String,
    val title: String = "",
)
```

Note the `url` is passed **as a plain string** here (unlike PDF URLs). That
is safe because these URLs come from `AboutDefaults` constants and don't
contain query strings that would confuse nav parsing.

---

## 2. What it displays

1. **Top bar** — back button + title.
2. **Full-screen web view** delegated to a platform-specific `expect`:

```kotlin
@Composable
expect fun PlatformWebView(url: String, modifier: Modifier = Modifier)
```

### Android
- Uses **Custom Tabs** (`androidx.browser:browser`) when possible — it is
  faster to boot, shares cookies with the user's default browser, and gives
  a familiar UX.
- Falls back to `WebView` if Custom Tabs is unavailable.

### iOS
- Wraps `WKWebView` inside a `UIKitView`.
- Configured with `allowsBackForwardNavigationGestures = true`.

---

## 3. Callback

Only one — `onBack()` → `navController.popBackStack()`.

---

## 4. Non-obvious details

- **No JavaScript bridge.** The web view is used for browsing external
  pages, not for hybrid features. There is no `addJavascriptInterface` /
  `WKScriptMessageHandler` code.
- **Deep-link URLs (rrbmustudies://…) are intentionally NOT routed through
  this screen.** They go through `AppDeepLinks.parseAppDeepLink` and are
  handled natively.
- **HTTP (non-HTTPS) URLs** may be rejected by Android's Network Security
  Config. In practice, everything on `AboutDefaults` should be HTTPS.
