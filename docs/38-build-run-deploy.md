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

# 38 — Build, Run and Deploy

Everything you need to build the app on your own machine and ship it.

---

## 1. Prerequisites

- **JDK 17** (Temurin or OpenJDK) on PATH.
- **Android Studio Ladybug** or newer with:
  - Android SDK 35 platform installed.
  - Android SDK Build-Tools 35.
  - Command-line Tools.
- **Xcode 15+** for iOS builds (macOS only).
- **Node.js 20+** for Cloud Functions and the seed script.
- **Firebase CLI** — `npm i -g firebase-tools`.
- **Ruby + CocoaPods** — if the iOS project uses Pods for Google Mobile Ads
  (not by default here since iOS ads are stubbed).

---

## 2. Local configuration

### 2.1 Android SDK path — `local.properties`

At the project root:

```
sdk.dir=/Users/you/Library/Android/sdk
```

(Android Studio writes this automatically on first import.)

### 2.2 AdMob IDs — `local.properties` (optional)

For release builds that actually earn:

```
admob.app.id=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
admob.banner.default=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
admob.interstitial.paper=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
admob.rewarded.offline=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
admob.rewarded.interstitial=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
```

If any of these are missing, the release build falls back to Google test IDs.

### 2.3 Firebase config files

- **Android** — put `google-services.json` in `composeApp/google-services.json`.
- **iOS** — put `GoogleService-Info.plist` inside the Xcode project (usually
  under the `iosApp` folder if generated via `iosMain`'s Kotlin project
  wrapper).

Neither of these are checked into git.

### 2.4 Firebase project alias — `.firebaserc`

Copy `.firebaserc.example` to `.firebaserc` and set your project ID:

```json
{
  "projects": {
    "default": "rrbmu-studies-your-alias"
  }
}
```

---

## 3. Build Android

### Debug

```
./gradlew :composeApp:assembleDebug
```
Output: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`.

### Release APK

```
./gradlew :composeApp:assembleRelease
```

### Release AAB (for Play Store)

```
./gradlew :composeApp:bundleRelease
```

Release builds require a signing config. Add this to
`composeApp/build.gradle.kts` under `android { … signingConfigs { … } }`
using values pulled from environment variables or `~/.gradle/gradle.properties`
— it is intentionally left out of the repo.

---

## 4. Run Android

- **Studio** — click the Run ▶ button after selecting an emulator or
  physical device.
- **Command line** — `./gradlew :composeApp:installDebug` then start the
  activity manually or use `adb shell am start`.

---

## 5. Build iOS

The iOS module builds a static framework called `ComposeApp`.

### Generate the framework
```
./gradlew :composeApp:linkPodDebugFrameworkIosSimulatorArm64
```
(or `linkPodReleaseFrameworkIosArm64` for a device release).

### Xcode

Open the Xcode project (usually generated under `iosApp/`) and:
1. Point it at `ComposeApp.framework` for the current configuration.
2. Ensure `GoogleService-Info.plist` is added.
3. Set your Team + Bundle ID.
4. Build & Run onto a simulator or device.

For automated CI, `xcodebuild` with the framework's build path works fine.

---

## 6. Firebase deploy

### Firestore + Storage rules and indexes

```
firebase deploy --only firestore:rules,firestore:indexes,storage
```

### Cloud Function

```
cd functions && npm install
firebase deploy --only functions
```

### Everything

```
firebase deploy
```

---

## 7. Seed the courses collection

Run once per project:

```
cd scripts
npm install
export GOOGLE_APPLICATION_CREDENTIALS=/absolute/path/to/service-account.json
node seed-courses.js
```

This creates 31 courses and for each of them creates the 3 systems and their
parts. Papers are **not** seeded — admins upload those in-app.

---

## 8. Testing

```
./gradlew allTests
```

Runs:
- `domain:commonTest` — including `CourseSeedTest`.
- `data:commonTest` — including `MappersTest`, `FirestorePathParserTest`.

There are no instrumented UI tests wired up currently.

---

## 9. Signing the Android build

Add this to `composeApp/build.gradle.kts` (real credentials come from your
environment):

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // …
        }
    }
}
```

**Do not** commit keystores. Use encrypted secrets in CI.

---

## 10. Publishing to the stores

### Google Play

1. Bump `versionCode` and `versionName` in `composeApp/build.gradle.kts`.
2. Build the AAB (`./gradlew :composeApp:bundleRelease`).
3. Upload to Play Console → Internal testing → Promote to Production.

### Apple App Store

1. Bump the build number in the iOS project.
2. Archive from Xcode.
3. Distribute via Transporter.
4. Submit for review via App Store Connect.

---

## 11. Known post-clone gotchas

- **First Android build fails** because `google-services.json` is missing.
  Fix: add the file, then invalidate caches & restart.
- **iOS build fails on device** because `iosArm64` framework isn't built.
  Fix: run the `linkPodReleaseFrameworkIosArm64` Gradle task.
- **Firebase Auth throws "operation not allowed"** — enable Email/Password
  in Firebase console → Authentication → Sign-in method.
- **Push not received** — confirm:
  - `google-services.json` matches the current Firebase project.
  - App has notification permission (Android 13+).
  - Device has subscribed to `all_users` (check via Firebase console
    → Cloud Messaging → send a topic message manually).

That is the whole build/run/deploy story.
