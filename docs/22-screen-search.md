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

# 22 — Screen: Search

**Files:**
- `presentation/search/SearchScreen.kt`
- `presentation/search/SearchViewModel.kt`

Global search across courses, subjects, and papers.

---

## 1. Route

```kotlin
@Serializable
data object SearchRoute
```

Reached by tapping the search bar on Home or Courses.

---

## 2. What it displays

1. **Top bar** — back button.
2. **`SearchBar`** (interactive, auto-focused) at the top.
3. **Filter tabs** — chips: `ALL | COURSES | SUBJECTS | PAPERS`.
4. **Results** based on the selected tab:
   - **Courses** — `CourseListRow`s.
   - **Subjects** — grouped list showing `subject → matching paper count`.
   - **Papers** — `PaperCard`s.
   - **All** — a merged section-headered layout.
5. **Empty state** with a hint ("Try a course name, subject, or paper code").
6. **Ad banner slot** at the bottom.

---

## 3. State — `SearchViewModel`

```kotlin
enum class SearchTab(val label: String) {
    ALL("All"), COURSES("Courses"), SUBJECTS("Subjects"), PAPERS("Papers"),
}

data class SearchUiData(
    val tab: SearchTab = SearchTab.ALL,
    val courses: List<Course> = emptyList(),
    val subjects: List<SubjectSearchHit> = emptyList(),
    val papers: List<Paper> = emptyList(),
    val isSearching: Boolean = false,
)

data class SubjectSearchHit(val subject: String, val paperCount: Int)
```

Injected use cases:
- `SearchPapersUseCase`
- `GetCoursesUseCase`
- `LogSearchUseCase`

### Query pipeline

```
input flow:  _query.debounce(300 ms).distinctUntilChanged()
      │
      ▼
each query fans out to:
   – GetCoursesUseCase() then filter client-side by name/shortName
   – SearchPapersUseCase(query, limit=100)
     – result grouped by subject → SubjectSearchHit list
```

Debouncing gives users room to keep typing without spamming Firestore.
`distinctUntilChanged()` prevents duplicate searches.

### Actions

- `onQueryChange(q: String)` — updates `_query`.
- `onTabChange(tab: SearchTab)` — updates `_tab`.
- `refresh()` — re-emits the current query.

Each successful non-empty query also fires `LogSearchUseCase(query)` to
analytics.

---

## 4. Callbacks

- `onBack()` — pop.
- `onPaperClick(paper)` — routes to `PaperDetailRoute` if the paper has full path info.
- `onCourseClick(course)` — routes to `CourseDetailRoute(course.id, course.name, course.shortName)`.

Tapping a subject in the SUBJECTS tab (or subject header in ALL) shows only
that subject's papers — implemented by setting the tab to PAPERS and
filtering internally.

---

## 5. How the paper search actually works

Under the hood, `SearchRepositoryImpl` → `FirestoreSearchDataSource`:

- Uses `firestore.collectionGroup("papers").where("isPublished", "==", true).limit(N)`
- Loads all matching papers.
- Filters *client-side*: any paper whose `title`, `subject`, `paperCode`, or
  `description` contains the query (case-insensitive).
- Sorts by `createdAt DESC`.

Rationale: Firestore has no native full-text search. Client-side filtering
scales to a few thousand papers and keeps the app free of extra dependencies
like Algolia. If the corpus grows, migrate to a search index.

---

## 6. Empty states

| Situation | UI |
|-----------|----|
| Query blank | `EmptyState("Start typing to search papers, subjects or courses")` |
| Query set, no results | `EmptyState("No results")` |
| Loading | small progress spinner in the search bar's trailing area |

---

## 7. Non-obvious details

- **Course filter is fully client-side**, since the full course list is small
  (~30 items).
- **Paper query is limited to 100** results per fetch to keep bandwidth sane.
- **Analytics event** `search` is logged with the query string; be mindful of
  PII rules if users could type personal data (unlikely here).
