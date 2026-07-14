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

# 23 — Screen: Notifications (Tab 3 of Main)

**Files:**
- `presentation/notifications/NotificationsScreen.kt`
- `presentation/notifications/NotificationsViewModel.kt`

Where students see everything the university has published. Also the source
of the unread badge on the top bar and bottom bar.

---

## 1. What it displays

1. **`SectionHeader`** — "Notifications".
2. **`SearchBar`** — filters by title/body.
3. **`FilterChipRow`** — chips: `ALL | ANNOUNCEMENT | TIME TABLE | IMPORTANT`.
4. **`LazyColumn` of `NotificationItem`s**.
5. **`EmptyNotificationState`** if nothing matches.
6. Pull-to-refresh.
7. **Ad banner slot** at the bottom.

Each `NotificationItem` shows title, body preview, a small `NotificationTypeBadge`,
and a red unread dot if the current user has not opened this notification.

---

## 2. State — `NotificationsViewModel`

```kotlin
enum class NotificationFilter { ALL, ANNOUNCEMENTS, TIME_TABLE, IMPORTANT }

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val query: String = "",
    val filter: NotificationFilter = NotificationFilter.ALL,
    val readIds: Set<String> = emptySet(),
) {
    val hasUnreadItems: Boolean
        get() = notifications.any { it.id !in readIds }
    fun filteredNotifications(): List<AppNotification> = ...
}

val hasUnread: StateFlow<Boolean>
```

Injected:
- `GetNotificationsUseCase` — flow of active notifications.
- `SettingsRepository` — for `readNotificationIds` flow and `markNotificationRead`.

### Load logic

```kotlin
init {
    viewModelScope.launch {
        combine(
            getNotificationsUseCase(),
            settingsRepository.readNotificationIds,
        ) { list, read -> list to read }
        .collect { (list, readIds) ->
            _state.update { it.copy(notifications = list, readIds = readIds) }
        }
    }
}
```

### Actions

- `onQueryChange(q)`
- `onFilterChange(filter)`
- `markRead(id: String)` — persists via `settingsRepository.markNotificationRead(id)`.
- `refresh()`

---

## 3. Filtering

- Query matches title or body substring, case-insensitive.
- Filter chip narrows by `NotificationCategory`.

Combined client-side; `getNotificationsUseCase()` returns them all in
`createdAt DESC` order.

---

## 4. Unread badge logic

- `readIds` is a `Set<String>` persisted in `multiplatform-settings` as a CSV.
- `hasUnread` is `true` if any active notification's id is not in `readIds`.
- When the user opens `NotificationDetailScreen`, the detail ViewModel calls
  `markRead(id)` on init, and the `readIds` flow emits, causing both the tab
  list and the top-bar badge to update automatically.
- To avoid unbounded growth, the settings layer caps the CSV to the most
  recent 300 IDs.

---

## 5. Where the shared instance comes from

Recall `MainScreen` calls `koinViewModel<NotificationsViewModel>()` **once**
and passes it into `NotificationsScreen` explicitly. That is because the
top bar needs `hasUnread` too. `NotificationsScreen` uses that same
instance via the parameter.

---

## 6. Empty and error states

- **No notifications at all** — friendly `EmptyNotificationState`.
- **No matches for filter/query** — `EmptyState("No notifications match")`.
- **Load error** — `ErrorState` with retry.

---

## 7. Callbacks

- `onNotificationClick(notification)` — routes to
  `NotificationDetailRoute(notification.id)`.
- `onOpenSettings()` — used by an optional gear in the top bar to open
  `SettingsRoute`.

---

## 8. Non-obvious details

- **Live updates.** Because the underlying Firestore query is a snapshot,
  a newly published notification appears at the top of the list without a
  refresh — and the badge lights up simultaneously.
- **FCM parity.** The same list you see here is what a push notification
  makes the user *tap into*. Tapping a push opens the same
  `NotificationDetailScreen` via the deep-link path (see 11-navigation.md).
