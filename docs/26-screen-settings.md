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

# 26 — Screen: Settings

**Files:**
- `presentation/settings/SettingsScreen.kt`
- `presentation/settings/SettingsViewModel.kt` (contains `SettingsViewModel` **and** `AppSettingsViewModel`)

The Settings screen is where users control the theme, manage downloaded
papers, and — importantly — where **admin sign-in** actually happens.

---

## 1. Route

```kotlin
@Serializable
data object SettingsRoute
```

Reached from the top bar's gear icon, from the "More" tab, and from any
"Login" prompt across the app.

---

## 2. What it displays

Sections top-to-bottom:

1. **Admin section** (`SettingsCard`):
   - If not signed in: a big **"Sign in as Admin"** row that opens the
     `AdminLoginBottomSheet`.
   - If signed in: the admin's email + display name, a **Sign Out** row,
     and admin-only management rows (Manage Home Ads, Manage Notifications).
2. **Appearance** (`SettingsCard`):
   - `ThemeSegmentedControl` (Light / Dark / System).
   - `SettingsToggle` for push notifications (currently updates a local
     preference only; a future version can hook into FCM subscription).
3. **Storage** (`SettingsCard`):
   - "Downloaded Papers" row → routes to `DownloadedPapersRoute`.
     Trailing subtitle shows `count · size` (from `VaultStorageInfo`).
   - "Clear downloaded papers" row → opens a confirmation dialog; on OK
     runs `ClearPdfVaultUseCase`.
4. **Legal** (`SettingsCard`):
   - Rows for Privacy Policy, Terms of Service, Disclaimer → each routes to
     `LegalDocumentRoute(documentId)`.

---

## 3. State — `SettingsViewModel`

```kotlin
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val pushNotificationsEnabled: Boolean = true,
    val downloadedPapersCount: Int = 0,
    val downloadedPapersSizeMb: String = "0 MB",
    val isAdmin: Boolean = false,
    val isAdminResolved: Boolean = false,
    val adminUser: AdminUser? = null,
    val showAdminLoginSheet: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showClearDownloadsDialog: Boolean = false,
    val isClearingDownloads: Boolean = false,
)

sealed interface SettingsEvent {
    data class Snackbar(val message: String) : SettingsEvent
}
```

Injected use cases:
- `ObserveThemeModeUseCase`, `SetThemeModeUseCase`
- `ObserveAdminStateUseCase`, `SignOutAdminUseCase`
- `ObserveVaultStorageUseCase`, `ClearPdfVaultUseCase`

### Load

Combines four flows:

```
combine(
    themeMode,          // Flow<ThemeMode>
    adminState.isAdmin, // Flow<Boolean>
    adminState.currentUser, // Flow<AdminUser?>
    observeVaultStorageUseCase(),  // Flow<VaultStorageInfo>
) { theme, isAdmin, user, storage ->
    SettingsUiState(
        themeMode = theme,
        isAdmin = isAdmin,
        isAdminResolved = true,   // now we know both true/false authoritatively
        adminUser = user,
        downloadedPapersCount = storage.count,
        downloadedPapersSizeMb = storage.sizeLabel,
    )
}.collect { _state.value = it }
```

### Actions

- `setThemeMode(mode: ThemeMode)` — persists via `SetThemeModeUseCase`.
- `setPushNotifications(enabled: Boolean)` — toggles a local flag.
- `showAdminLoginSheet()` / `dismissAdminLoginSheet()`.
- `showLogoutDialog()` / `dismissLogoutDialog()` / `confirmLogout()` — calls
  `SignOutAdminUseCase`, emits Snackbar "Signed out".
- `showClearDownloadsDialog()` / `dismissClearDownloadsDialog()`.
- `clearDownloadedPapers()` — sets `isClearingDownloads = true`, calls
  `ClearPdfVaultUseCase`, then emits Snackbar "All downloads removed" or
  the error message.

---

## 4. `AppSettingsViewModel`

Separate ViewModel that just exposes `themeMode: StateFlow<ThemeMode>` and
`setThemeMode()`. It is registered as a singleton in the presentation module
and consumed by `App.kt` (the root Composable) so the whole app can
re-theme without going through `SettingsViewModel`.

---

## 5. Callbacks (from `AppNavHost`)

```kotlin
SettingsScreen(
    onBack = { navController.popBackStack() },
    onManageAds = { navController.navigate(ManageHomeAdsRoute) },
    onManageNotifications = { navController.navigate(ManageNotificationsAdminRoute) },
    onDownloadedPapers = { navController.navigate(DownloadedPapersRoute) },
)
```

Legal rows navigate to `LegalDocumentRoute(id)` where id is one of
`"privacy"`, `"terms"`, `"disclaimer"` (see `AboutDefaults.legalDocuments`).

---

## 6. Non-obvious details

- **Admin login is a bottom sheet, not a route.** Rationale: staying on
  Settings after login means the admin sees their menu options unfold
  in-place. Only if the user actually wants a full-screen login flow do
  they use `AdminLoginRoute` (accessed from the About screen).
- **`isAdminResolved`** — one extra Boolean so the UI can show a placeholder
  instead of the "guest" state during the ~50 ms while the auth flow emits
  its first value. Prevents a "flash of unauthenticated content".
- **Push toggle** — currently local only. The FCM subscription for the
  `all_users` topic is unconditional in `RrbmuMessagingService`, so
  disabling this toggle *does not yet* unsubscribe. This is a known TODO.
