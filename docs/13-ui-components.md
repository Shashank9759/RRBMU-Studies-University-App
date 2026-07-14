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

# 13 — UI Components Library

Under `composeApp/src/commonMain/kotlin/com/studies/rrbmustudies/ui/components/`
there are ~23 files, most containing several composables. These are the
building blocks the screens are made of.

This document lists **every reusable composable**, what it does, and where it
is used.

---

## 1. `RrbmuTopBar.kt`
A `TopAppBar` variant used across most screens.
- Takes an optional back arrow, screen title, and up to a couple of trailing
  icons (search, notifications bell, settings).
- Uses the "Stitch" surface tone rather than the primary brand colour so it
  reads as a chrome layer, not a hero banner.
- Used by: `MainScreen`, `CourseDetailScreen`, `PartPapersScreen`,
  `SearchScreen`, `SettingsScreen`, `AboutTeamScreen`, all admin screens.

## 2. `BottomBar.kt` — `RrbmuBottomBar`
The 4-tab navigation bar at the bottom of `MainScreen`.
- Tabs: Home, Courses, Notifications, More.
- Shows a red dot badge on the Notifications tab when `hasUnread`.
- Uses Material 3's `NavigationBar` under the hood.

## 3. `SearchBar.kt`
A rounded search input row with an inner `leading` search icon and a
`trailing` clear-X button. Two modes:
- **Interactive** — full `TextField`, used on `SearchScreen`.
- **Read-only** — behaves like a tappable button, used on `HomeScreen` and
  `CoursesScreen` (tapping it routes to `SearchRoute`).

## 4. `FilterChip.kt`
Wraps Material `FilterChip` with the app's styling.
- Exposes `FilterChipRow` — a horizontal scrollable row of chips.
- Also a `FilterChipPrimaryContainer` variant for the "All / UG / PG /
  Diploma" filter above the Courses list.

## 5. `SectionHeader.kt`
Left-aligned title (e.g. "Select Course", "Recently Added Papers") with
an optional right-hand `TextButton` (e.g. "See all"). Used on Home,
Courses and Settings.

## 6. `EmptyState.kt`
A vertically centred column with an icon, headline, and body text used
whenever a list is empty (no papers, no notifications, no downloads). Also
exports `ErrorState` which adds a "Retry" button.

## 7. `LoadingShimmer.kt`
A shimmering placeholder box using an animated linear gradient. Used to
show loading skeletons for course tiles, paper cards and notifications.

## 8. `AdCarousel.kt`
Auto-scrolling ad carousel with dot indicator.
- Takes a list of `HomeAd` + `HomeCarouselSettings`.
- Uses a `Pager` internally; pauses on user drag; loops indefinitely.
- Falls back gracefully if the list has only one item (no scroll).

## 9. `AdBannerSlot.kt`
A wrapper around the `expect fun AdMobBanner()` composable. Shows a native
AdMob banner on Android and nothing on iOS (`No-Op`). Used at the bottom of
many screens (Notifications, Settings, Downloaded, More…).

## 10. `PaperCard.kt`
Card for a single paper. Includes:
- Subject-coloured left bar (deterministic from subject name).
- Title, subject, code, year row.
- Download counter and, when relevant, "downloaded ✓" badge.
- Used in `PartPapersScreen`, `SearchScreen`, and (as a compact variant)
  on `HomeScreen`.

## 11. `CourseCard.kt`
Multiple composables in one file:
- `CourseCard` — Home screen grid tile (icon + short name).
- `QuickAccessCourseCard` — smaller Home variant.
- `CourseListRow` — Courses tab list row with level chip.
- `LevelSectionHeader` — "Undergraduate / Postgraduate / Diploma" title.
- `RecentPaperCard` — Home "Recently Added" card.
- `PartExploreCard` — Course-detail card representing a system's part.

## 12. `StitchBreadcrumb.kt`
The small `Home > Course > Year 1` bar shown on drill-down screens. Takes an
ordered list of `String` labels; separates them with `chevron` icons.

## 13. `NotificationComponents.kt`
- `NotificationItem` — list row with title/body/date and unread dot.
- `NotificationTypeBadge` — pill showing category (Announcement /
  Time Table / Important).
- `EmptyNotificationState` — friendlier empty state for the tab.

## 14. `SettingsComponents.kt`
- `SettingsCard` — grouped rows container (rounded card with tinted surface).
- `SettingsRow` — one row with leading icon, title, optional subtitle and
  trailing content.
- `SettingsSectionHeader` — small ALL CAPS section title.
- `SettingsToggle` — row + trailing `Switch`.
- `ThemeSegmentedControl` — Light / Dark / System 3-button segmented picker.

## 15. `MoreComponents.kt`
- `MoreMenuRow` — a big pill-shaped row used on More tab.
- `MoreSectionHeader`
- `GuestUserCard` — the "Signed in as…" or "Sign in" call-out at the top of
  the More screen.

## 16. `FeedbackComponents.kt`
- `EmojiRatingBar` — 5-emoji rating selector.
- `FeedbackTagChipRow` — selectable multi-tag chip strip.

## 17. `FeedbackHeroIllustration.kt`
A vector-based hero illustration for the feedback screen.

## 18. `TeamMemberCard.kt`
Card used on About screen for each team member (photo, name, role, socials).

## 19. `AdminBadge.kt`
Small purple badge shown next to the app name when the current user is an
admin (Home top bar, About header).

## 20. `AdminFab.kt`
Floating action button shown to admins on Course/Part/Home screens.
Reads `LocalIsAdmin.current`; renders nothing when not admin. Icon is a
plus. Used for "upload paper" (part screen) and "create course" (course
list).

## 21. `AdminLoginBottomSheet.kt`
Modal bottom sheet with an email + password form. Used from
`SettingsScreen` — clicking "Admin login" opens this sheet instead of a
full-page login. It runs the same `AdminLoginViewModel` as the standalone
login screen.

## 22. `RrbmuDialog.kt`
A themed wrapper around Material `AlertDialog` for confirmation dialogs
(logout, clear downloads, delete ad/notification).

## 23. `BrandLogo.kt`
Renders the university logo image (`Res.drawable.univ_logo`). Sized modifier
optional. Used on splash and top bar.

---

## Cross-referencing to screens

| Component | First appears on |
|-----------|-----------------|
| `BrandLogo` | Splash |
| `RrbmuTopBar` + `RrbmuBottomBar` | Main scaffold |
| `SearchBar` (read-only) + `AdCarousel` + `SectionHeader` + `CourseCard` + `RecentPaperCard` | Home |
| `FilterChipRow` + `CourseListRow` + `LevelSectionHeader` | Courses tab |
| `StitchBreadcrumb` + `PartExploreCard` | Course Detail |
| `PaperCard` | Part papers |
| `AdminFab` | Course + Part + Dashboard (admin only) |
| `NotificationItem` + `NotificationTypeBadge` + `EmptyNotificationState` | Notifications |
| `MoreMenuRow` + `GuestUserCard` | More tab |
| `SettingsCard`, `SettingsRow`, `ThemeSegmentedControl` | Settings |
| `EmojiRatingBar` + `FeedbackTagChipRow` + `FeedbackHeroIllustration` | Feedback |
| `TeamMemberCard` | About |
| `AdminLoginBottomSheet` | Settings (admin login) |
| `AdBannerSlot` | Nearly every content screen |
| `LoadingShimmer` | Home, Courses, Part papers, Notifications while loading |

Once you know the components, reading any screen file is straightforward —
each one is essentially a `Column { … }` of these building blocks.
