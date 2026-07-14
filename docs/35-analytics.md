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

# 35 — Analytics

Analytics uses **Firebase Analytics** through the GitLive multiplatform SDK.
It is intentionally minimal — three events, no user IDs.

---

## 1. Events

| Event | Parameters | When fired |
|-------|------------|-----------|
| `paper_view` | `paper_id`, `course_id` | `PaperDetailViewModel.refresh()` after successful load |
| `paper_download` | `paper_id`, `course_id` | `PaperDetailViewModel.trackDownloadOnce()` on successful vault download |
| `search` | `query` | `SearchViewModel` after each non-empty query resolves |

That's it. No custom screen views, no purchase events, no user properties.

---

## 2. Where they live in code

Interface: `domain/repository/AnalyticsRepository.kt`

```kotlin
interface AnalyticsRepository {
    fun logPaperView(paperId: String, courseId: String)
    fun logDownload(paperId: String, courseId: String)
    fun logSearch(query: String)
}
```

Use cases (`domain/usecase/AnalyticsUseCases.kt`):

```kotlin
class LogPaperViewUseCase(private val repo: AnalyticsRepository) {
    operator fun invoke(paperId: String, courseId: String) = repo.logPaperView(paperId, courseId)
}
class LogDownloadUseCase(private val repo: AnalyticsRepository) {
    operator fun invoke(paperId: String, courseId: String) = repo.logDownload(paperId, courseId)
}
class LogSearchUseCase(private val repo: AnalyticsRepository) {
    operator fun invoke(query: String) = repo.logSearch(query)
}
```

Implementation: `data/repository/FirebaseAnalyticsRepository.kt`. Forwards
straight to GitLive's `Firebase.analytics.logEvent(...)`.

---

## 3. What Firebase does with those events

- Aggregates them under the standard Analytics dashboards
  (Events → Real-time, Engagement → Events).
- No BigQuery export is configured by default.
- No consent management is implemented (the app is aimed at university
  students in India; GDPR concerns are limited but should be reviewed if
  the app is ever offered in the EU).

---

## 4. Non-obvious details

- **Paper views are logged on load, not on open.** Opening the PDF is a
  separate action; the view is defined as "the user landed on the paper
  detail screen".
- **Search queries** are truncated / trimmed but are otherwise sent as-is.
  Beware of PII if UX ever changes (unlikely — the search box explicitly
  asks for course/subject/code).
- **No screen tracking.** If you need per-screen dashboards, wire a
  `LaunchedEffect(Unit) { FirebaseAnalytics.logEvent("screen_view", ...) }`
  inside each screen. Not currently in place.
