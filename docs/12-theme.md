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

# 12 — Theme and Design System

The app has a Material 3 based theme called **"Stitch"** (internal name).
This file explains every ingredient.

Location: `composeApp/src/commonMain/kotlin/com/studies/rrbmustudies/ui/theme/`.

---

## 1. Files

| File | What it holds |
|------|---------------|
| `Color.kt` | Every colour token (primary, surface, subject accents…) |
| `Type.kt` | Typography scale |
| `Shape.kt` | Rounded corner shapes |
| `Dimens.kt` | Spacing / padding / size constants |
| `Theme.kt` | `RrbmuTheme` composable — resolves light vs dark scheme |
| `AdminLocals.kt` | `LocalIsAdmin`, `LocalAdminUser` composition locals |
| `AboutDefaults.kt` | App branding constants, contact info, legal docs |
| `AppConfig.kt` | App-wide feature toggles (currently minimal) |
| `StitchExtras.kt` | Extra theme extensions used by a few components |

---

## 2. Colours — `Color.kt`

### 2.1 Brand primary
- `StitchPrimary = 0xFF000666` (deep navy blue) — main brand colour
- `StitchOnPrimary = 0xFFFFFFFF`
- `StitchPrimaryContainer = 0xFF1A237E`
- `StitchOnPrimaryContainer = 0xFF8690EE`
- `StitchPrimaryFixed = 0xFFE0E0FF` (for fixed-tone components)

### 2.2 Secondary (warm accent)
- `StitchSecondary = 0xFF9F4200` (burnt orange)
- `StitchSecondaryContainer = 0xFFFD6C00`
- `StitchSecondaryFixed = 0xFFFFDBCB`

### 2.3 Tertiary (support blue)
- `StitchTertiary = 0xFF000F5B`
- `StitchTertiaryContainer = 0xFF072189`

### 2.4 Surfaces (light)
- `StitchSurface = 0xFFF8F9FC`
- `StitchSurfaceContainerLowest = 0xFFFFFFFF`
- `StitchSurfaceContainerLow = 0xFFF2F3F6`
- `StitchSurfaceContainer = 0xFFEDEEF1`
- `StitchSurfaceContainerHigh = 0xFFE7E8EB`
- `StitchSurfaceVariant = 0xFFE1E2E5`

### 2.5 Surfaces (dark)
- `StitchDarkSurface = 0xFF121212`
- `StitchDarkSurfaceContainerLow = 0xFF191C1E`
- `StitchDarkSurfaceContainer = 0xFF1E1F24`
- `StitchDarkSurfaceContainerHigh = 0xFF282A2F`
- `StitchDarkOnSurface = 0xFFE1E2E5`
- `StitchDarkPrimary = 0xFFBDC2FF`

### 2.6 Text / outlines
- `StitchOnSurface = 0xFF191C1E`
- `StitchOnSurfaceVariant = 0xFF454652`
- `StitchOutline = 0xFF767683`
- `StitchOutlineVariant = 0xFFC6C5D4`

### 2.7 Errors
- `StitchError = 0xFFBA1A1A`
- `StitchErrorContainer = 0xFFFFDAD6`

### 2.8 Subject accents (used on paper cards for a coloured left bar)
- `SubjectBlue = 0xFF3B82F6`
- `SubjectIndigo = 0xFF6366F1`
- `SubjectEmerald = 0xFF10B981`
- `SubjectAmber = 0xFFF59E0B`
- `SubjectRose = 0xFFF43F5E`
- `SubjectSky = 0xFF0EA5E9`

These are picked deterministically from the subject name so the same subject
always gets the same colour.

### 2.9 Shimmer colours (for loading placeholders)
- `ShimmerBase = 0xFFF0F2F8`, `ShimmerHighlight = 0xFFE2E6F0`

---

## 3. Typography — `Type.kt`

The type scale follows Material 3's standard slots. Font family
`FontFamily.SansSerif` (i.e. the platform's default sans font — Roboto on
Android, SF on iOS). Sizes broadly:

- Display / Headline — used for hero banners and section titles.
- Title — card titles, dialog titles.
- Body — main content.
- Label — buttons, chips, small captions.

The scale defines `titleMedium`, `titleLarge`, `bodyMedium`, `bodyLarge`,
`labelLarge`, `labelMedium`, `headlineMedium`, `headlineLarge`, `displayLarge`.

---

## 4. Shapes — `Shape.kt`

Rounded corner values for cards, buttons and dialogs. Common values are
`8.dp`, `12.dp`, `16.dp`, `24.dp`. Components pick from these instead of
hardcoding.

---

## 5. Dimensions — `Dimens.kt`

An `object Dimens` holding named spacing constants: `xxs`, `xs`, `sm`, `md`,
`lg`, `xl`, plus larger radii and elevation. Every screen uses these instead
of literal `dp` values, so the whole app can be re-spaced by editing one file.

---

## 6. The theme composable — `Theme.kt`

```kotlin
@Composable
fun RrbmuTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (darkTheme) darkColorScheme(...) else lightColorScheme(...)
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
```

`ThemeMode` comes from `SettingsRepository.themeMode` and is persisted in
Settings (see 09-local-storage.md).

`App.kt` uses it like this:
```kotlin
val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
RrbmuTheme(themeMode = themeMode) { AppNavHost(...) }
```

The Settings screen has a segmented control (`ThemeSegmentedControl`
component) that writes back via `SetThemeModeUseCase`.

---

## 7. Composition locals — `AdminLocals.kt`

```kotlin
val LocalIsAdmin = compositionLocalOf { false }
val LocalAdminUser = compositionLocalOf<AdminUser?> { null }
```

`AppNavHost` sets these based on `AdminViewModel`'s state:

```kotlin
CompositionLocalProvider(
    LocalIsAdmin provides isAdmin,
    LocalAdminUser provides adminUser,
) { NavHost(...) }
```

Any composable can read them:

```kotlin
val isAdmin = LocalIsAdmin.current
if (isAdmin) AdminFab(...)
```

This is how components (like a course card, or a paper list) show admin
affordances without receiving `isAdmin` as an explicit parameter.

---

## 8. `AboutDefaults.kt` — brand constants

Holds:
- `APP_NAME`, `APP_TAGLINE` — used on splash + about.
- `WHATSAPP_E164`, `EMAIL`, `PHONE_TEL` — contact URIs.
- Team member list (name, role, photo URL).
- Legal document map: `legalDocuments["privacy"] = "…policy text…"`.

The About and Legal screens pull directly from here.

---

## 9. `AppConfig.kt` and `StitchExtras.kt`

`AppConfig.kt` — a small collection of top-level constants used across the
app (currently mostly booleans / flags — some may be no-ops).

`StitchExtras.kt` — extension properties on `ColorScheme` for extra tokens
that Material 3 doesn't have (e.g. subject accent selector, "subtle" text).

---

## 10. Dark / light behaviour summary

- Both schemes are hand-tuned; no dynamic colour is used.
- Every widget that would look wrong in dark mode is given an explicit
  dark counterpart (see the "Dark" section in `Color.kt`).
- Images (e.g. course backgrounds) are dimmed with an overlay in dark mode.
- Shimmer colours have separate light/dark values.

That is the design system.
