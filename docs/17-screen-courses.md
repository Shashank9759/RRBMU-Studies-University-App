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

# 17 — Screen: Courses (Tab 2 of Main)

**Files:**
- `presentation/courses/CoursesScreens.kt` (the `CoursesScreen` composable)
- `presentation/courses/CoursesViewModels.kt` (the `CoursesViewModel`)

The full catalogue of every course RRBMU offers. It is a flat, searchable and
filter-able list — not a hierarchy.

---

## 1. What it displays

1. **`SearchBar`** at the top (interactive) — filters courses by `name` and
   `shortName` client-side.
2. **`FilterChipRow`** — chips: `All | UG | PG | Diploma`.
3. **Grouped list** using `LevelSectionHeader` and `CourseListRow`:
   - Section: "Undergraduate" (all `UG` courses)
   - Section: "Postgraduate" (all `PG` courses)
   - Section: "Diploma" (all `DIPLOMA` courses)
4. **`EmptyState`** if the filter/search produces nothing.
5. Pull-to-refresh enabled.

If an admin is logged in, the `AdminFab` shows at the bottom right so they
can jump to the "Manage Courses" screen — but only via composition local, not
prop-drilled.

---

## 2. State — `CoursesViewModel`

```kotlin
data class CoursesUiState(
    val courses: List<Course> = emptyList(),
    val searchQuery: String = "",
    val selectedLevel: CourseLevel? = null,
) {
    val filteredCourses: List<Course>
        get() = courses
            .filter { selectedLevel == null || it.level == selectedLevel }
            .filter {
                searchQuery.isBlank() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.shortName.contains(searchQuery, ignoreCase = true)
            }
}
```

Use case: `GetCoursesUseCase`.

The full course list is fetched once (as a Flow) and stored in `state`.
Filtering is entirely client-side, so typing in the search bar is instant.

### Actions

- `onSearchQueryChange(q: String)` — updates state.
- `onLevelFilter(level: CourseLevel?)` — updates state.
- `refresh()` — re-emit the Firestore flow.

---

## 3. Grouping

The screen groups `filteredCourses` by `CourseLevel` into three sections
(UG, PG, Diploma) and renders each with a `LevelSectionHeader`. Empty
sections are simply skipped.

Order within each section respects `Course.order` from Firestore.

---

## 4. Callbacks

- `onCourseClick(course)` — routes to `CourseDetailRoute(course.id, course.name, course.shortName)`.
  Passing the name/short-name lets the detail screen render before it has
  received the course document, avoiding a flicker.

---

## 5. Admin overlay

Admin can:
- Tap the FAB → goes to `ManageCoursesAdminRoute` (fully separate screen).
- On the course detail (next screen), see per-course editing tools.

There is no editing capability *on this list itself* — Manage Courses is a
distinct screen (32-screens-admin.md).

---

## 6. Empty and error states

- **No courses at all** — `EmptyState("No courses yet")`.
- **Filter/search matches nothing** — `EmptyState("No courses match")`.
- **Load error** — `ErrorState` with a retry button.

Because `CourseRepositoryImpl.getAllCourses()` falls back to `CourseSeed`
locally on error, the "no courses at all" state should be rare in practice.
