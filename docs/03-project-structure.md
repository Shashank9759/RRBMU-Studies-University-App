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

# 03 — Project Structure

This file explains what every folder and Gradle module in the project is for.
Read it side by side with your file explorer for a solid mental map.

---

## Root layout

```
RRBMU-Studies-University-App/
├── composeApp/            # UI module (the actual app for Android + iOS)
├── domain/                # Pure Kotlin business logic (no framework)
├── data/                  # Data sources, DTOs, repository implementations
├── shared/                # DI wiring only (Koin init)
├── firebase/              # Firestore/Storage security rules + setup docs
├── functions/             # Node.js Cloud Function that sends push on new notifications
├── scripts/               # Node.js seed script for courses
├── gradle/                # Version catalog (libs.versions.toml) + wrapper
├── build/                 # Gradle build outputs (ignore)
├── .idea/                 # IntelliJ / Android Studio project files
├── firebase.json          # Firebase project config (rules paths, functions folder)
├── .firebaserc.example    # Sample Firebase project alias
├── local.properties       # SDK path + AdMob IDs (gitignored)
└── README.md              # (currently just the title)
```

---

## Gradle modules

There are exactly four Gradle modules.

### 1. `composeApp/` — the app

Contains all UI, screens, view models, navigation, ads glue, and platform-specific entry points.

```
composeApp/src/
├── commonMain/kotlin/com/studies/rrbmustudies/
│   ├── App.kt                 # Root composable
│   ├── ads/                   # Common Ads interface
│   ├── di/                    # PresentationModule (Koin)
│   ├── navigation/            # Routes, AppNavHost, deep links, PDF codec
│   ├── platform/              # expect fns (PlatformServices, NotificationPermission)
│   ├── presentation/          # Every screen + view model, grouped by feature
│   │   ├── splash/
│   │   ├── main/
│   │   ├── home/
│   │   ├── courses/
│   │   ├── paper/
│   │   ├── search/
│   │   ├── notifications/
│   │   ├── feedback/
│   │   ├── settings/
│   │   ├── more/
│   │   └── admin/
│   └── ui/
│       ├── components/        # 24+ reusable composables
│       ├── theme/             # Color, Type, Shape, Dimens, Theme
│       ├── state/             # Sealed UiState<T>
│       └── util/              # Error mapping helpers
├── commonMain/composeResources/
│   └── drawable/univ_logo.png # University logo used on splash + top bar
├── androidMain/kotlin/…       # MainActivity, ads impl, FCM service, pickers
├── androidMain/res/           # AndroidManifest, launcher icons, drawables
└── iosMain/kotlin/…           # MainViewController + iOS pickers/stubs
```

### 2. `domain/` — pure Kotlin business logic

No Compose, no Android, no Firebase — this module could be re-used in a Ktor
backend or a desktop client.

```
domain/src/commonMain/kotlin/com/studies/rrbmustudies/domain/
├── model/          # Course, Paper, Part, HomeAd, AppNotification, Feedback, Enums…
├── repository/     # Abstract repo interfaces (CourseRepository, PaperRepository, …)
├── usecase/        # ~47 use case classes (one per repo method, callable as invoke)
└── (commonTest)/   # CourseSeedTest.kt — sanity-check seed data
```

### 3. `data/` — repository implementations + data sources

Everything that talks to Firebase or the local database lives here.

```
data/src/commonMain/kotlin/com/studies/rrbmustudies/data/
├── dto/                   # @Serializable DTOs mirroring Firestore documents
├── mapper/                # DTO ↔ domain-model mappers
├── remote/                # Abstract *RemoteDataSource interfaces
│   └── firestore/         # GitLive Firebase implementations
├── local/                 # SQLDelight-backed local data sources
├── repository/            # …RepositoryImpl classes
├── di/                    # DataModule (Koin) + PlatformModule expect
└── platform/              # PdfVaultFileStore expect
data/src/commonMain/sqldelight/com/studies/rrbmustudies/data/local/
├── CachedPaper.sq         # Recently viewed papers cache
└── DownloadedPaper.sq     # Downloaded PDF metadata
data/src/androidMain/…     # SQLDelight Android driver, Firebase init, file store
data/src/iosMain/…         # Same but for iOS
```

### 4. `shared/` — Koin bootstrap

A very small module. Its only job is to hold `KoinInit.kt`, so that both
Android and iOS can start Koin the same way.

```
shared/src/commonMain/kotlin/com/studies/rrbmustudies/di/
├── KoinInit.kt
└── DomainModule.kt        # Koin module registering all use cases
```

---

## Package convention

Every module uses the base package **`com.studies.rrbmustudies`** with a
sub-package for its layer, e.g.:

- `com.studies.rrbmustudies` (composeApp entry)
- `com.studies.rrbmustudies.presentation.home`
- `com.studies.rrbmustudies.domain.model`
- `com.studies.rrbmustudies.data.repository`

This mirrors the folder structure exactly.

---

## Non-Kotlin folders

### `firebase/`

Holds Firestore & Storage security rules plus setup docs:

```
firebase/
├── firestore.rules
├── firestore.indexes.json
├── storage.rules
├── FIREBASE_SETUP.md
└── FCM_SETUP.md
```

### `functions/`

One Node.js Cloud Function that reacts to writes on `notifications/{id}` and
sends a push to the `all_users` FCM topic.

```
functions/
├── index.js
├── package.json
└── package-lock.json
```

### `scripts/`

Node script to seed the 31 built-in courses into Firestore.

```
scripts/
├── seed-courses.js
├── package.json
└── package-lock.json
```

### `gradle/`

Standard Gradle wrapper plus the version catalog:

```
gradle/
├── libs.versions.toml
└── wrapper/
    ├── gradle-wrapper.jar
    └── gradle-wrapper.properties
```

### Config files at root

| File | Purpose |
|------|---------|
| `firebase.json` | Tells the Firebase CLI where rules, indexes and functions live. |
| `.firebaserc.example` | Sample of `.firebaserc` (which contains your Firebase project alias). |
| `local.properties` | SDK path + AdMob IDs. **Not** checked into git. |
| `git.ignore` | Alt gitignore file (rename to `.gitignore` if needed). |

---

## How the modules depend on each other

```
composeApp ──► domain
composeApp ──► data
composeApp ──► shared
shared     ──► domain
shared     ──► data
data       ──► domain
domain     ──► (nothing — pure Kotlin only)
```

- `domain` is at the bottom and depends on nothing.
- `data` implements `domain`'s interfaces.
- `shared` glues both together via Koin.
- `composeApp` uses everything.

This dependency direction is what makes it a *clean* architecture. UI can be
swapped, data sources can be swapped, but the domain never changes.
