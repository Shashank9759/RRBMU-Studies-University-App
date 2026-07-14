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

# 16 — Screen: Home (Tab 1 of Main)

**Files:**
- `presentation/home/HomeScreen.kt`
- `presentation/home/HomeViewModel.kt`

The default landing tab. Its job is to give the user quick access to the
things they most likely came for: a course, a recent paper, or a promo/ad.

---

## 1. What it displays (top to bottom)

1. **Read-only Search bar** — tapping anywhere opens `SearchRoute`.
2. **Ad carousel** — auto-scrolling promo banners (falls back to
   `HomeDefaults.promoAds` if Firestore has none).
3. **Ad banner slot** — native AdMob banner (Android only; no-op on iOS).
4. **"Select Course" section header** with a "View All" button that switches
   the tab to `BottomTab.Courses`.
5. **2-column grid of courses** (`QuickAccessCourseCard`). Empty state if
   there are no active courses.
6. **"Recently Added" section header**.
7. **Horizontally scrolling row of `RecentPaperCard`s**. Empty state when
   there are none.

The whole content is inside a `LazyColumn` wrapped in a `Box` with
`pullRefresh` so users can pull down to reload.

---

## 2. State — `HomeViewModel`

Location: `presentation/home/HomeViewModel.kt`.

```kotlin
data class HomeUiState(
    val ads: List<HomeAd> = HomeDefaults.promoAds,
    val courses: List<Course> = emptyList(),
    val recentPapers: List<Paper> = emptyList(),
    val carouselSettings: HomeCarouselSettings = HomeCarouselSettings(),
)

val state: StateFlow<UiState<HomeUiState>>
```

Injected use cases:
- `GetHomeAdsUseCase` (or `GetActiveHomeAdsUseCase`)
- `GetCoursesUseCase`
- `GetRecentPapersUseCase`
- `ObserveHomeCarouselSettingsUseCase`
- `ObserveAdminStateUseCase` — when the user is an admin, all ads (including
  drafts) are shown so an admin can preview them.

### Load logic

```kotlin
init { load() }

fun refresh() = load()

private fun load() {
    viewModelScope.launch {
        _state.value = UiState.Loading
        combine(
            adsSource, coursesSource, recentPapersSource, carouselSettings
        ) { ads, courses, recent, settings ->
            HomeUiState(ads, courses, recent, settings)
        }
        .catch { _state.value = UiState.Error(it.toUserMessage()) }
        .collect { _state.value = UiState.Success(it) }
    }
}
```

The combine emits **every time any of the four flows changes** — so an admin
uploading a new paper causes the Home screen to re-render live.

---

## 3. Callbacks (passed in from `MainScreen`)

- `onCourseClick(course)` — navigates to `CourseDetailRoute`.
- `onPaperClick(paper)` — navigates to `PaperDetailRoute` if the paper has
  full path info.
- `onSearchClick()` — navigates to `SearchRoute`.
- `onAdClick(ad)` — validates the URL is `http://` or `https://`, then opens
  it in the browser via `openUrl()`.
- `onSeeAllCourses()` — switches `MainScreen`'s tab to `BottomTab.Courses`.

---

## 4. Pull-to-refresh

Uses the Compose Material 1 `pullRefresh` API:

```kotlin
val pullRefreshState = rememberPullRefreshState(
    refreshing = state is UiState.Loading,
    onRefresh = viewModel::refresh,
)
```

Because the underlying flows are Firestore snapshots, `refresh()` is almost
always instant — the data is already fresh. The gesture is there mostly to
give users a way to recover after a network error.

---

## 5. States handled

| State | UI |
|-------|----|
| `Loading` | Full-screen `LoadingShimmer` |
| `Error` | `ErrorState` with a retry button |
| `Success(data)` | Full layout described above |

Empty-list cases render `EmptyState` inline for each section rather than
failing the whole screen.

---

## 6. Non-obvious details

- **Default ads.** If Firestore returns no active ads (fresh project or offline),
  `HomeAdRepositoryImpl` falls back to `HomeDefaults.promoAds`, so the
  carousel is never empty.
- **Course grid.** Built manually with `Column { Row { CourseCard, CourseCard } }`
  rather than `LazyVerticalGrid` because it's inside a `LazyColumn`
  (nesting scrollables would break it). Uses `chunked(2)` for pairing and a
  `Spacer(weight = 1f)` when the count is odd.
- **Recent papers scroll horizontally.** Same reason — nested scrollables
  need explicit `horizontalScroll(rememberScrollState())`.

That is the Home tab.
