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

# 33 — Ads (AdMob Integration)

The app runs Google AdMob on **Android only**. iOS has a no-op stub. This
document explains where ads appear, how the ad IDs are configured, and how
the ad gating logic decides when to show something.

---

## 1. Where ads appear

Two kinds of ads are used.

### 1.1 Banner ads
Rendered via `AdBannerSlot()` (`ui/components/AdBannerSlot.kt`), which is a
thin wrapper around the `expect fun AdMobBanner()`.

Banner slots appear at the bottom of most content screens:
- Home
- Search
- Notifications
- Notification detail
- Paper detail
- Settings
- Downloaded papers
- More

Bottom banners collapse to zero height when the ad fails to load or on
iOS (no-op impl).

### 1.2 Interstitial / rewarded ads
Full-screen ads shown *before* certain user actions on paper detail:

- **Opening a paper's PDF** (`openPdf()`).
- **Downloading a paper for offline reading** (`downloadToVault()`).

These go through `PaperAdGateway`.

---

## 2. `PaperAdGateway`

Location (interface): `composeApp/src/commonMain/kotlin/com/studies/rrbmustudies/ads/Ads.kt`.

```kotlin
interface PaperAdGateway {
    fun requestBeforeOpenPaper(isAdmin: Boolean, proceed: () -> Unit)
    fun requestBeforeOfflineDownload(isAdmin: Boolean, proceed: () -> Unit)
}
```

Rules the interface encodes:
- **Always call `proceed()`** — even if no ad plays. The lambda is the "user
  should now see the PDF" callback.
- **Admins never see ads.** The `isAdmin` parameter causes the gateway to
  skip straight to `proceed()`.

### Default binding

`presentationModule` registers a **no-op** implementation as a fallback:

```kotlin
single<PaperAdGateway> { NoOpPaperAdGateway }

object NoOpPaperAdGateway : PaperAdGateway {
    override fun requestBeforeOpenPaper(isAdmin: Boolean, proceed: () -> Unit) = proceed()
    override fun requestBeforeOfflineDownload(isAdmin: Boolean, proceed: () -> Unit) = proceed()
}
```

Because Koin has `allowOverride(true)`, the Android module can replace it.

### Android binding

`composeApp/androidMain/di/AndroidAdsModule.kt` overrides the binding to
a real gateway backed by AdMob. Roughly:

- **`requestBeforeOpenPaper(isAdmin) { proceed }`**
  - if admin → `proceed()`.
  - increment `paperOpenCount` in Settings.
  - check throttling — see below.
  - if not throttled, show a **rewarded interstitial**. On close call
    `proceed()`. On failure to load, `proceed()`.
- **`requestBeforeOfflineDownload(isAdmin) { proceed }`**
  - if admin → `proceed()`.
  - show a **rewarded ad** (opt-in with "watch to download"). On earn-reward
    or close call `proceed()`; set `skipNextInterstitial = true` so the
    subsequent open-paper action doesn't burn *another* ad.

### Throttling

`SettingsRepository` exposes:
- `getLastFullScreenAdAt()` / `setLastFullScreenAdAt(ts)`
- `getSkipNextInterstitial()` / `setSkipNextInterstitial(v)`
- `getPaperOpenCount()` / `incrementPaperOpenCount()`

Approximate policy in the Android gateway:
- If `skipNextInterstitial == true`, consume it and skip.
- If `now - lastFullScreenAdAt < COOLDOWN_MS` (e.g. 90 s), skip.
- If `paperOpenCount % 3 != 0` (i.e. show on every third open), skip.
- Otherwise show, update `lastFullScreenAdAt = now`.

Exact constants live in `AndroidAdManager` — the point of documenting the
policy here is that ads are **not** shown every time; they respect a
frequency cap and give admins & recent viewers a break.

---

## 3. AdMob IDs

Five IDs are needed (see `composeApp/build.gradle.kts`):
- `admob.app.id` — the AdMob application ID (goes into AndroidManifest).
- `admob.banner.default`
- `admob.interstitial.paper`
- `admob.rewarded.offline`
- `admob.rewarded.interstitial`

### Debug builds
Always use Google's public test IDs (`ca-app-pub-3940256099942544/…`).
These are documented at
`developers.google.com/admob/android/test-ads` — they always fill and
never earn revenue.

### Release builds
Look up the real IDs from `local.properties`. If a key is missing, fall
back to the test ID. This makes it safe to build a "release" locally
without real ad IDs — it just doesn't earn.

The Gradle logic:

```kotlin
manifestPlaceholders["admobAppId"] = adProp("admob.app.id", testAdmobAppId)
buildConfigField("String", "ADMOB_BANNER",
    "\"${adProp("admob.banner.default", testBannerId)}\"")
// … and so on for interstitial / rewarded / rewarded-interstitial
```

---

## 4. `AndroidAdManager`

`composeApp/androidMain/ads/AndroidAdManager.kt`. Singleton (Koin-bound).

Responsibilities:
- Initialise the Mobile Ads SDK once at app start (`MobileAds.initialize(context)`).
- Pre-load interstitial and rewarded ads so they're ready to show without a
  visible loading step.
- Provide `showInterstitial(activity, onDone)` and `showRewarded(activity,
  onEarn, onClose)` methods.
- Track load failures and back off (don't retry immediately in a tight loop).

The manager needs an `Activity` to actually show a full-screen ad, so it
uses the `ActivityHolder` platform helper (`platform/ActivityHolder.kt`).
`MainActivity` writes itself into `ActivityHolder` in `onResume` and
clears in `onPause`.

---

## 5. `AdMobBanner` — the Compose banner

Common `expect`:
```kotlin
@Composable expect fun AdMobBanner(modifier: Modifier = Modifier)
```

Android impl (`AdMobBanner.android.kt`) wraps `AdView` inside `AndroidView`,
sets `AdSize.SMART_BANNER`, and applies the `admob.banner.default` unit ID.

iOS impl (`Ads.ios.kt`) is a no-op (`Spacer(Modifier.height(0.dp))`).

---

## 6. iOS

No AdMob SDK on iOS. Both `AdMobBanner()` and the default `PaperAdGateway`
short-circuit to their no-op paths. Adding iOS AdMob later would only need:

1. Add Google Mobile Ads via CocoaPods to the iOS project.
2. Provide a real `iosMain` `AdMobBanner` and `IosAdManager` + `IosAdsModule`.
3. Call `initKoin(listOf(iosAdsModule))` from `MainViewController`.

---

## 7. Testing recommendations

- Use the Google test IDs during local development.
- Add your own test device ID via
  `MobileAds.setRequestConfiguration(RequestConfiguration.Builder().setTestDeviceIds([...]).build())`
  when working with real IDs.
- Never ship a debug build to a store — the test IDs are baked in.
