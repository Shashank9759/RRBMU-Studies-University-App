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

# 18 — Screen: Course Detail

**Files:**
- `presentation/courses/CoursesScreens.kt` (`CourseDetailScreen` composable)
- `presentation/courses/CoursesViewModels.kt` (`CourseDetailViewModel`)

Reached by tapping a course from Home or the Courses tab.

---

## 1. Route

```kotlin
@Serializable
data class CourseDetailRoute(
    val courseId: String,
    val courseName: String = "",
    val courseShortName: String = "",
)
```

Both `courseName` and `courseShortName` are passed as *display fallbacks* so
the top bar shows the right text before the course document arrives.

---

## 2. What it displays

1. **Top bar** — back button + course short name.
2. **Hero banner** — the course's `backgroundImageUrl` (Coil) with a gradient
   overlay and the full name centred.
3. **`StitchBreadcrumb`** — e.g. `Home > B.A.`.
4. **System selector chips** — Yearly / Semester / Entrance (only the ones
   that exist for this course are shown).
5. **List of parts** for the selected system, using `PartExploreCard`:
   - Each card shows the part name, paper count, and an arrow.
   - Tapping navigates to `PartPapersRoute`.
6. **`AdminFab`** — visible to admins only, opens `UploadPaperRoute`
   pre-filled with `courseId`.

---

## 3. State — `CourseDetailViewModel`

Constructor parameters (via Koin `parametersOf`):
- `courseId: String`
- `courseName: String`

Use cases injected:
- `GetCourseUseCase`
- `GetCourseSystemsUseCase`
- `GetPartsUseCase`

```kotlin
data class CourseDetailUiState(
    val courseName: String,
    val backgroundImageUrl: String? = null,
    val systems: List<CourseSystem> = emptyList(),
    val selectedSystemType: SystemType = SystemType.YEARLY,
    val parts: List<Part> = emptyList(),
)
```

### Load logic

```kotlin
init { load() }

private fun load() {
    viewModelScope.launch {
        combine(
            getCourse(courseId),          // Flow<Course?>
            getSystems(courseId)           // Flow<List<CourseSystem>>
        ) { course, systems -> course to systems }
        .flatMapLatest { (course, systems) ->
            val selectedSystem = systems.firstOrNull { it.type == selectedSystemType }
                ?: systems.firstOrNull()
            getParts(courseId, selectedSystem?.id ?: return@flatMapLatest emptyFlow())
                .map { parts -> Triple(course, systems, parts) }
        }
        .collect { (course, systems, parts) ->
            _state.value = UiState.Success(
                CourseDetailUiState(
                    courseName = course?.name ?: initialName,
                    backgroundImageUrl = course?.backgroundImageUrl,
                    systems = systems,
                    selectedSystemType = _selectedType,
                    parts = parts,
                )
            )
        }
    }
}
```

If the requested system type has no parts (or doesn't exist for this
course), the ViewModel auto-switches to the first available system so the
UI never shows an empty list on load.

### Actions

- `onSystemTypeSelected(type)` — updates `_selectedType`, re-triggers the
  parts flow.
- `refresh()` — restart the whole combine.

---

## 4. Callbacks

`CourseDetailScreen`:

- `onBack()` — `navController.popBackStack()`.
- `onPartClick(systemId, partId, partName, systemType)` — routes to
  `PartPapersRoute(courseId, systemId, partId, partName, courseShortName, systemLabel)`.
  `systemLabel` is derived from `systemType.name.lowercased().capitalized()`.

---

## 5. States handled

| State | UI |
|-------|----|
| Loading | Shimmer over the banner + list |
| Error | `ErrorState` |
| Success — no systems | Message: "No content added yet" |
| Success — systems present | Full chip + list layout |

---

## 6. Non-obvious details

- The **hero image** uses Coil's crossfade so switching between courses feels
  smooth.
- **System chips** are chosen from `SystemType` enum values, but only chips
  for systems that actually exist (`systems.map { it.type }`) are rendered.
- The **paper count** on `PartExploreCard` is the `Part.paperCount` field,
  which is denormalised in Firestore for speed — you do not have to actually
  count papers.
