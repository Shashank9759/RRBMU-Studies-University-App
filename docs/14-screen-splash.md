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

# 14 — Screen: Splash

**File:** `composeApp/src/commonMain/kotlin/com/studies/rrbmustudies/presentation/splash/SplashScreen.kt`

The first screen the user ever sees. It exists only to give the app a moment
to warm up (initialize Firebase, kick off Koin, start collecting Auth flows).

---

## 1. What it displays

- Full-screen background: `UniversityBlue` (deep navy).
- Centered `BrandLogo` (the university logo), 84 dp tall.
- Below it: **"RRBMU Studies"** in `headlineLarge`.
- Below that: **"University Question Papers"** in `bodyLarge` (85% opacity).

Nothing else — no progress spinner, no buttons.

---

## 2. What it does

```kotlin
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(800)
        onFinished()
    }
    // …UI…
}
```

A single `LaunchedEffect(Unit)` waits 800 ms and calls `onFinished()`.
`Unit` as the key means the effect runs exactly once when the composable
enters the composition.

---

## 3. Where the callback goes

Inside `AppNavHost.kt`:

```kotlin
composable<SplashRoute> {
    SplashScreen(
        onFinished = {
            splashFinished = true
            val link = initialDeepLink
            if (link != null) {
                handledDeepLinkKey = deepLinkKey(link)
                openDeepLink(link, fromSplash = true)   // navigate to deep-linked screen
            } else {
                navController.navigate(MainRoute) {
                    popUpTo(SplashRoute) { inclusive = true }
                }
            }
        },
    )
}
```

Two outcomes:
1. **No deep link** → navigate to `MainRoute`, popping the splash off the back stack.
2. **Deep link present** (user tapped a push or a share URL) → follow the link.

---

## 4. ViewModel

None. This screen has no state and no dependencies — it is a pure UI + timer.

---

## 5. Why 800 ms?

- Short enough not to feel slow.
- Long enough for `AdminViewModel` to have received one emission from
  `AuthRepository.isAdmin`, so `LocalIsAdmin` is already correct by the
  time `MainScreen` renders. Without the delay, the first frame of Home
  would sometimes show non-admin state to an admin, then flip.
