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

# 39 — Glossary

Every acronym, brand name, and term used throughout these docs and the
codebase, in one place.

---

### Kotlin / Multiplatform

- **KMP** — Kotlin Multiplatform. Kotlin's compiler produces both JVM
  bytecode (Android) and native binaries (iOS) from one codebase.
- **CMP** — Compose Multiplatform. JetBrains' extension of Jetpack Compose
  that runs on Android, iOS, desktop and wasm.
- **`expect` / `actual`** — the KMP mechanism for platform-specific bindings.
  `expect` declares an API in common code; `actual` implements it per
  platform.
- **KSP** — Kotlin Symbol Processing. A lightweight annotation processor
  used by SQLDelight.
- **AGP** — Android Gradle Plugin.
- **DSL** — Domain Specific Language.
- **Compose** — JetBrains / Google declarative UI toolkit.
- **Compose runtime / foundation / material3** — three layers of Compose we
  depend on.

### Architecture

- **Clean Architecture** — the layered pattern (Presentation → Domain →
  Data) used by this app.
- **MVVM** — Model-View-ViewModel. In practice, a Composable View collects
  state from a ViewModel exposed as `StateFlow<UiState<T>>`.
- **UiState** — the sealed interface (`Loading | Success<T> | Error`) that
  every screen collects.
- **Use case** — a class wrapping one repository method; injected into
  ViewModels for testability.
- **Repository** — an abstract layer that hides remote and local data
  sources behind a domain API.
- **DTO** — Data Transfer Object. Firestore-shaped `@Serializable` classes;
  mapped to/from domain models.
- **Mapper** — extension functions that convert DTOs to domain models and
  back.
- **DI** — Dependency Injection. This project uses **Koin**.
- **Koin module** — a container that binds interfaces to implementations
  (`single`, `factory`, `viewModel`).

### Firebase

- **Firestore** — Firebase's NoSQL document database.
- **Firebase Storage** — object storage for PDFs and images.
- **Firebase Auth** — email/password authentication.
- **Firebase Analytics** — event tracking dashboard.
- **FCM** — Firebase Cloud Messaging. Push notifications.
- **Cloud Function** — Node.js function triggered by Firestore writes.
- **GitLive Firebase SDK** — third-party wrapper that exposes Firebase to
  KMP `commonMain`.
- **Security rules** — declarative access-control policy for Firestore and
  Storage. See `firebase/firestore.rules` and `firebase/storage.rules`.
- **Composite index** — an index that supports multi-field queries (e.g.
  `isPublished + createdAt DESC`).

### Local storage

- **SQLDelight** — compile-time-safe SQL library for Kotlin. Generates
  `RrbmuDatabase` and query classes.
- **`.sq` file** — SQLDelight's source format.
- **multiplatform-settings** — Russhwolf's cross-platform key/value store
  (SharedPreferences on Android, NSUserDefaults on iOS).
- **`ObservableSettings`** — a Settings variant that exposes changes as Flows.
- **PDF Vault** — the app's private folder + DB table for offline paper PDFs.

### Ads

- **AdMob** — Google's mobile advertising platform.
- **Banner ad** — small persistent ad at the bottom of a screen.
- **Interstitial ad** — full-screen ad shown between actions.
- **Rewarded ad** — user opts in to watch, receives some benefit.
- **Rewarded interstitial** — hybrid; can be shown unpromoted like an
  interstitial with optional reward.
- **`PaperAdGateway`** — the common interface that decides whether to show
  an ad before opening or downloading a paper.

### Navigation

- **Type-safe route** — a `@Serializable data class/object` used as a
  destination key. Replaces string routes.
- **Deep link** — a URI that opens the app to a specific screen.
- **`rrbmustudies://…`** — the app's custom URI scheme.
- **`PdfNavCodec`** — Base64-URL-safe encoding wrapper for PDF sources.

### Domain concepts

- **Course** — a programme of study (e.g. B.A., M.Sc.).
- **CourseSystem** — a way of splitting a course into parts. Three types:
  YEARLY, SEMESTER, ENTRANCE.
- **Part** — a division within a system (e.g. "Semester 3", "Year 1").
- **Paper** — a past examination question paper. PDF + metadata.
- **Home ad** — a promotional banner shown on the Home carousel.
- **Notification** — an announcement written by an admin, delivered by FCM.
- **Feedback** — a rating/comment submitted by a student.
- **Admin** — a user whose UID exists in the Firestore `admins` collection.

### Misc

- **RRBMU** — Raj Rishi Bhartrihari Matsya University (the target university).
- **UG / PG / Diploma** — Undergraduate / Postgraduate / Diploma course
  levels.
- **`asia-south1`** — the Firebase region used for Firestore and Cloud
  Functions.
- **`all_users`** — the FCM topic every install subscribes to.

Once you understand these terms, every part of the codebase reads clearly.
