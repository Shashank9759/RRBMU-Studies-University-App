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

# 04 — Gradle Build Configuration

Everything about how the project is built: version catalog, per-module
`build.gradle.kts` files, KMP targets, Android config, ad IDs, ProGuard.

---

## 1. The version catalog: `gradle/libs.versions.toml`

Everyone shares one catalog. It has three sections:

- `[versions]` — plain version strings (e.g. `kotlin = "2.1.0"`).
- `[libraries]` — dependencies. Referenced in Kotlin as `libs.koin.core`.
- `[plugins]` — Gradle plugins. Referenced as `libs.plugins.kotlin.multiplatform`.

Every `build.gradle.kts` uses `alias(libs.plugins.xxx)` and
`implementation(libs.xxx)` instead of hardcoded version strings. Bumping a
version happens in one place.

See **02-tech-stack.md** for the full list of what is in the catalog.

---

## 2. `composeApp/build.gradle.kts` — the app module

### Plugins applied
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}
```

### KMP targets
- **Android target** → JVM 17.
- **iOS X64** (Intel simulator), **iOS Arm64** (device), **iOS Simulator Arm64** (Apple Silicon simulator).
- Each iOS target produces a **static framework** named `ComposeApp` — this is
  what the iOS project embeds.

### Source-set dependencies

- **commonMain** — Compose runtime + foundation + material3, Navigation Compose,
  Koin, kotlinx-serialization, Coil, and `project(":domain")`, `project(":shared")`.
- **androidMain** — Activity-Compose, Core-KTX, Koin-Android, PDF viewer,
  Custom Tabs, native Firebase Messaging, Play Services Ads, Coil OkHttp engine,
  and `project(":data")`.
- **iosMain** — Coil Ktor, Ktor Darwin engine.

Notice that only `androidMain` depends on `:data`. This is because
`shared/DomainModule` and `PlatformModule` also depend on `:data`, so common
code sees repository interfaces through `:domain`.

### Android block
```kotlin
android {
    namespace   = "com.studies.rrbmustudies"
    compileSdk  = 35
    defaultConfig {
        applicationId = "com.studies.rrbmustudies"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = 2
        versionName   = "2.0.0"
    }
    buildFeatures { buildConfig = true }
}
```

Java toolchain is 17 (`sourceCompatibility` and `targetCompatibility`).

### AdMob IDs — debug vs. release

At the top of the file, five Google test IDs are declared as fallbacks:
```
testAdmobAppId               = ca-app-pub-3940256099942544~3347511713
testBannerId                 = ca-app-pub-3940256099942544/6300978111
testInterstitialId           = ca-app-pub-3940256099942544/1033173712
testRewardedId               = ca-app-pub-3940256099942544/5224354917
testRewardedInterstitialId   = ca-app-pub-3940256099942544/5354046379
```

**Debug builds** always use these test IDs.

**Release builds** look up real IDs from `local.properties` using keys:
- `admob.app.id`
- `admob.banner.default`
- `admob.interstitial.paper`
- `admob.rewarded.offline`
- `admob.rewarded.interstitial`

If any key is missing, it falls back to the corresponding test ID. This is
what the helper does:

```kotlin
fun adProp(key: String, fallback: String): String =
    (localProps.getProperty(key) ?: "").ifBlank { fallback }
```

Whichever value is chosen is written into `BuildConfig` (Kotlin) *and*
into the AndroidManifest via `manifestPlaceholders["admobAppId"]`.

### ProGuard

Only release enables minification:
```kotlin
release {
    isMinifyEnabled = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
    )
}
```

### Compose tooling

`debugImplementation(compose.uiTooling)` enables the preview panel and
inspector in Android Studio for debug builds only.

---

## 3. `data/build.gradle.kts` — data module

### Plugins
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}
```

### KMP targets
Same as composeApp: `androidTarget`, `iosX64`, `iosArm64`, `iosSimulatorArm64`.
iOS framework named `Data`.

### Source-set dependencies
- **commonMain** — depends on `:domain`, coroutines, serialization,
  kotlinx-datetime, koin-core, six Firebase libs (`app`, `auth`, `firestore`,
  `storage`, `messaging`, `analytics`), SQLDelight runtime + coroutines,
  multiplatform-settings + coroutines.
- **androidMain** — `sqldelight-android`, `koin-android`, coroutines-android.
- **iosMain** — `sqldelight-native`.

### SQLDelight database declaration
```kotlin
sqldelight {
    databases {
        create("RrbmuDatabase") {
            packageName.set("com.studies.rrbmustudies.data.local")
        }
    }
}
```

This finds all `.sq` files in `data/src/commonMain/sqldelight/…` and generates
a `RrbmuDatabase` class plus type-safe query classes into the given package.

---

## 4. `domain/build.gradle.kts` — domain module

- Plugins: `kotlin.multiplatform` + `android.library`.
- **Depends on nothing but coroutines-core** — that is on purpose.
- iOS framework named `Domain`.

Because it has zero framework dependencies, this module could compile on any
JVM or native target and could be reused unchanged by, say, a Ktor backend.

---

## 5. `shared/build.gradle.kts` — DI wiring module

- Plugins: `kotlin.multiplatform` + `android.library`.
- Depends on `:domain`, `:data`, and Koin.
- Exposes `KoinInit.kt` so Android and iOS both call the same bootstrap.
- iOS framework named `Shared`.

---

## 6. `settings.gradle.kts` (implicit)

The four Gradle modules are included. Repositories declared: Google, Maven
Central, and JitPack (`com.github.mhiew:android-pdf-viewer` comes from JitPack).

---

## 7. `local.properties`

Not checked into git. On your machine it contains:

```
sdk.dir=/Users/…/Library/Android/sdk
admob.app.id=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
admob.banner.default=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
admob.interstitial.paper=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
admob.rewarded.offline=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
admob.rewarded.interstitial=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
```

If you omit the AdMob keys the release build silently uses Google test IDs
(so it won't earn revenue but will not crash).

---

## 8. AndroidManifest — the important bits

Located at `composeApp/src/androidMain/AndroidManifest.xml`.

- Application id: `com.studies.rrbmustudies`.
- Uses permissions: `INTERNET`, `POST_NOTIFICATIONS`.
- Registers `MainActivity` as the launcher.
- Adds two intent-filters for deep links:
  - `rrbmustudies://notification/{id}`
  - `rrbmustudies://paper/{courseId}/{systemId}/{partId}/{paperId}`
- Registers `RrbmuMessagingService` as the FCM receiver.
- Declares the default notification channel id `notifications`.
- Adds a `<meta-data>` tag for the AdMob application ID, using the placeholder
  filled by Gradle.

---

## 9. Building the app

- **Android debug** — `./gradlew :composeApp:assembleDebug`
- **Android release APK** — `./gradlew :composeApp:assembleRelease`
- **Android release AAB** — `./gradlew :composeApp:bundleRelease`
- **iOS framework** — `./gradlew :composeApp:linkPodDebugFrameworkIos*` or
  build from Xcode via the shipped Xcode project (see 38-build-run-deploy.md).
- **Run unit tests** — `./gradlew allTests` runs `domain`'s common tests too.

That covers the build system end to end.
