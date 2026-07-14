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

# 02 — Tech Stack

Every library, framework, and tool used in this project, with a short "why".
Version numbers are pulled from `gradle/libs.versions.toml`.

---

## Languages and platforms

| Item | Version | Purpose |
|------|---------|---------|
| **Kotlin** | 2.1.0 | The one language across all layers. |
| **Kotlin Multiplatform (KMP)** | (part of Kotlin 2.1) | Lets us share code between Android and iOS. |
| **Compose Multiplatform (CMP)** | 1.7.3 | UI toolkit. Same declarative Compose UI runs on both platforms. |
| **Compose Compiler plugin** | 1.5.15 (Kotlin plugin) | Handles Compose transformations. |
| **Android Gradle Plugin (AGP)** | 8.7.3 | Builds the Android app + libraries. |
| **KSP** | 2.1.0-1.0.29 | Kotlin Symbol Processing, needed by SQLDelight and some Compose code-gen. |
| **Google Services plugin** | 4.4.2 | Reads `google-services.json` and wires Firebase config into the Android build. |

Targets: `androidTarget`, `iosX64`, `iosArm64`, `iosSimulatorArm64`.
Android minSdk = 26, targetSdk = 35. Java toolchain = 17.

---

## UI layer

| Library | Version | Why |
|---------|---------|-----|
| `androidx.activity:activity-compose` | 1.9.3 | Compose entry point on Android. |
| `androidx.compose:compose-runtime/foundation/material3` | (Compose 1.7.3) | Base UI primitives, Material 3 components. |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` | 2.8.4 | `viewModel { … }` API in Compose. |
| `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose` | 2.8.4 | `collectAsStateWithLifecycle()` for safe Flow collection. |
| `org.jetbrains.androidx.navigation:navigation-compose` | 2.8.0-alpha10 | Type-safe navigation using `@Serializable` routes. |
| `io.coil-kt.coil3:coil-compose` | 3.0.4 | Async image loading (course tiles, ad banners, notifications). |
| `io.coil-kt.coil3:coil-network-ktor3` | 3.0.4 | Ktor backend for Coil so it works on iOS too. |

---

## Dependency injection

| Library | Version | Why |
|---------|---------|-----|
| `io.insert-koin:koin-core` | 4.0.0 | Core Koin DSL. |
| `io.insert-koin:koin-compose` | 4.0.0 | `KoinContext { … }` for Compose. |
| `io.insert-koin:koin-compose-viewmodel` | 4.0.0 | `koinViewModel<T>()` composable helper. |
| `io.insert-koin:koin-android` | 4.0.0 | Android-specific Koin extensions (Application scope). |

Koin is chosen over Hilt because Hilt does not support Kotlin Multiplatform.

---

## Coroutines and serialization

| Library | Version | Why |
|---------|---------|-----|
| `kotlinx-coroutines-core` | 1.9.0 | Structured concurrency, Flows. |
| `kotlinx-coroutines-android` | 1.9.0 | `Dispatchers.Main` on Android. |
| `kotlinx-serialization-json` | 1.7.3 | Turn DTOs to/from JSON and Firestore documents. |
| `kotlinx-datetime` | 0.6.1 | Cross-platform date/time (rarely used; timestamps are mostly `Long` epoch ms). |

---

## Firebase (GitLive KMP SDK)

All Firebase calls in `commonMain` go through the [GitLive](https://github.com/GitLiveApp/firebase-kotlin-sdk)
SDK, which wraps the native Firebase SDKs on both platforms.

| Library | Version | Purpose |
|---------|---------|---------|
| `dev.gitlive:firebase-app` | 2.1.0 | Init & config. |
| `dev.gitlive:firebase-auth` | 2.1.0 | Admin email/password login. |
| `dev.gitlive:firebase-firestore` | 2.1.0 | Main DB (courses, papers, ads, notifications, feedback). |
| `dev.gitlive:firebase-storage` | 2.1.0 | PDF and image hosting. |
| `dev.gitlive:firebase-messaging` | 2.1.0 | FCM (push notifications). |
| `dev.gitlive:firebase-analytics` | 2.1.0 | Event tracking (paper views, downloads, searches). |

There is also a **native** Firebase Messaging dependency on Android only, for
implementing the `FirebaseMessagingService`:

| Library | Version | Purpose |
|---------|---------|---------|
| `com.google.firebase:firebase-messaging` | 24.1.0 | Android-side `RrbmuMessagingService`. |

---

## Local storage

| Library | Version | Purpose |
|---------|---------|---------|
| `app.cash.sqldelight:runtime` | 2.0.2 | Type-safe SQL DB, KMP-friendly. |
| `app.cash.sqldelight:coroutines-extensions` | 2.0.2 | Flow support for queries. |
| `app.cash.sqldelight:android-driver` | 2.0.2 | SQLite driver for Android. |
| `app.cash.sqldelight:native-driver` | 2.0.2 | SQLite driver for iOS (bundled sqlite). |
| `com.russhwolf:multiplatform-settings` | 1.3.0 | Key-value storage (theme mode, read notification IDs, ad counters). |
| `com.russhwolf:multiplatform-settings-coroutines` | 1.3.0 | Flow wrapper around Settings. |

SQLDelight stores structured data (downloaded paper metadata + recently viewed
cache). Settings stores simple preferences.

---

## PDF viewing

| Library | Version | Purpose |
|---------|---------|---------|
| `com.github.mhiew:android-pdf-viewer` | 3.2.0-beta.3 | Android PDF rendering inside a Compose `AndroidView`. |

On iOS, PDF viewing is delegated to native `PDFView` (part of PDFKit) via an
`expect`/`actual` composable.

---

## Web + browser

| Library | Version | Purpose |
|---------|---------|---------|
| `androidx.browser:browser` | 1.8.0 | Custom Tabs for the "WebView" route on Android. |

---

## Ads

| Item | Purpose |
|------|---------|
| Google Mobile Ads SDK (Android only) | Banner, interstitial, rewarded and rewarded-interstitial ads. |

Ad unit IDs are pulled from `local.properties` at build time and exposed to the
Android app through `BuildConfig` fields:
`ADMOB_BANNER_ID`, `ADMOB_INTERSTITIAL_ID`, `ADMOB_REWARDED_ID`,
`ADMOB_REWARDED_INTERSTITIAL_ID`, `ADMOB_APP_ID`.

The common code has an `Ads` interface. Android provides the real implementation.
iOS provides a no-op stub (no ads on iOS in this build).

---

## Firebase backend extras

| Item | Purpose |
|------|---------|
| Cloud Functions (Node.js) | One function that runs when a notification doc is written and pushes an FCM message to the `all_users` topic. |
| Node seed script | `scripts/seed-courses.js` bulk-loads the 31 built-in courses into Firestore. |

---

## Kotlin plugins used in Gradle

| Plugin | Purpose |
|--------|---------|
| `org.jetbrains.kotlin.multiplatform` | Enables KMP. |
| `org.jetbrains.kotlin.android` | Enables Kotlin on Android. |
| `org.jetbrains.kotlin.plugin.serialization` | Generates serializers for `@Serializable` classes. |
| `org.jetbrains.kotlin.plugin.compose` | Compose compiler for Kotlin 2.x. |
| `org.jetbrains.compose` | Compose Multiplatform DSL. |
| `com.android.application` | Android app plugin (used by `composeApp`). |
| `com.android.library` | Android library plugin (used by `data`, `domain`, `shared`). |
| `app.cash.sqldelight` | Generates code from `.sq` files. |
| `com.google.devtools.ksp` | Required by SQLDelight. |
| `com.google.gms.google-services` | Wires Firebase config into the Android build. |

---

## What is intentionally NOT used

- **No Ktor client, no Retrofit.** All networking goes through Firebase SDKs.
- **No Room.** SQLDelight is the KMP-friendly replacement.
- **No Hilt / Dagger.** Koin is used instead (KMP-friendly).
- **No Jetpack Compose Navigation-XML.** Type-safe `@Serializable` routes only.
- **No RxJava.** Coroutines + Flow everywhere.
- **No custom server.** Firestore + Cloud Functions handle all backend logic.

That is the entire tech landscape of the app.
