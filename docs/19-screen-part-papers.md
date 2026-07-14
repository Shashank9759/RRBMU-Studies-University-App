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

# 19 — Screen: Part Papers (List of Papers in a Part)

**Files:**
- `presentation/courses/CoursesScreens.kt` (`PartPapersScreen`)
- `presentation/courses/CoursesViewModels.kt` (`PartPapersViewModel`)

Reached by tapping a Part card in `CourseDetailScreen`.

---

## 1. Route

```kotlin
@Serializable
data class PartPapersRoute(
    val courseId: String,
    val systemId: String,
    val partId: String,
    val partName: String = "",
    val courseShortName: String = "",
    val systemLabel: String = "Yearly",
)
```

The extra fields are display hints — the actual title comes from the route
args because we don't need to re-fetch the part just for its name.

---

## 2. What it displays

1. **Top bar** — back button + part name.
2. **`StitchBreadcrumb`** — e.g. `Home > B.A. > Yearly > Year 1`.
3. **Filter row** — two rows of chips:
   - **Subject filter** (`FilterChipRow`) — populated from `availableSubjects`
   - **Year filter** — chips for each distinct year present.
4. **`LazyColumn` of `PaperCard`s**, one per paper, grouped visually by year
   (year headers between groups).
5. **`AdminFab`** — routes to `UploadPaperRoute(courseId, systemId, partId)`.
6. Pull-to-refresh.

---

## 3. State — `PartPapersViewModel`

Constructor:
```kotlin
class PartPapersViewModel(
    private val getPapers: GetPapersUseCase,
    private val courseId: String,
    private val systemId: String,
    private val partId: String,
    val partName: String,
    private val isAdmin: Boolean,
) : ViewModel()
```

```kotlin
data class PartPapersUiState(
    val partName: String,
    val papers: List<Paper> = emptyList(),
    val subjectFilter: String = "",
    val yearFilter: Int? = null,
    val availableSubjects: List<String> = emptyList(),
    val availableYears: List<Int> = emptyList(),
) {
    val filteredPapers: List<Paper>
        get() = papers
            .filter { subjectFilter.isBlank() || it.subject.equals(subjectFilter, ignoreCase = true) }
            .filter { yearFilter == null || it.year == yearFilter }
}
```

### Loading

The ViewModel calls `GetPapersUseCase(courseId, systemId, partId, isAdmin = isAdmin)`
which returns `Flow<List<Paper>>`. Admins see unpublished (draft) papers too.

Once loaded, `availableSubjects` and `availableYears` are derived from
distinct values in the list — so the chip strip only shows filters that
actually match something.

### Actions

- `onSubjectFilterChange(subject: String)`
- `onYearFilterChange(year: Int?)`
- `refresh()`

---

## 4. Callbacks

- `onBack()` — pop.
- `onPaperClick(paperId)` — routes to
  `PaperDetailRoute(courseId, systemId, partId, paperId)`.
- `onUploadClick()` — routes to `UploadPaperRoute(courseId, systemId, partId)`
  (admin only, gated on `isAdmin`).

---

## 5. Filtering is client-side

There is no server-side query for subject or year — the ViewModel loads the
part's full paper list once and filters in memory. Rationale:

- A typical part has fewer than 50 papers.
- It makes chip filters instant.
- It avoids Firestore composite-index complexity.

If a part ever holds hundreds of papers, this can be revisited.

---

## 6. Empty and error states

- **No papers at all** — `EmptyState("No papers yet")`.
- **Filter matches nothing** — `EmptyState("No papers match your filters")`.
- **Error** — `ErrorState`.

---

## 7. Non-obvious details

- **Admins see drafts.** The `isPublished == false` papers are only in the
  flow if `isAdmin` was `true` at construction time. If the user logs in as
  admin *after* opening this screen, they need to back out and re-enter for
  drafts to appear.
- **`isAdmin` is passed via route args** rather than composition local
  because ViewModels are constructed *before* CompositionLocalProvider runs.
