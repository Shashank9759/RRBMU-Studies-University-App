package com.studies.rrbmustudies.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.AppNotification
import com.studies.rrbmustudies.domain.model.NotificationCategory
import com.studies.rrbmustudies.domain.repository.SettingsRepository
import com.studies.rrbmustudies.domain.usecase.GetNotificationsUseCase
import com.studies.rrbmustudies.ui.state.UiState
import com.studies.rrbmustudies.ui.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NotificationFilter(val label: String) {
    ALL("All"),
    ANNOUNCEMENTS("Announcements"),
    TIME_TABLE("Time Table"),
    IMPORTANT("Important"),
}

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val query: String = "",
    val filter: NotificationFilter = NotificationFilter.ALL,
    val readIds: Set<String> = emptySet(),
) {
    val hasUnread: Boolean
        get() = notifications.any { it.id !in readIds }
}

class NotificationsViewModel(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(NotificationFilter.ALL)

    private val _listState = MutableStateFlow<UiState<NotificationsUiState>>(UiState.Loading)
    val state: StateFlow<UiState<NotificationsUiState>> = _listState.asStateFlow()

    val hasUnread: StateFlow<Boolean> = state
        .map { ui -> (ui as? UiState.Success)?.data?.hasUnread == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _listState.value = UiState.Loading
            combine(
                getNotificationsUseCase().catch { emit(emptyList()) },
                settingsRepository.readNotificationIds,
                query,
                filter,
            ) { notifications, readIds, q, f ->
                NotificationsUiState(
                    notifications = notifications,
                    query = q,
                    filter = f,
                    readIds = readIds,
                )
            }.catch { e ->
                _listState.value = UiState.Error(
                    e.toUserMessage("Couldn't load alerts. Pull to refresh."),
                ) { refresh() }
            }.collect { data ->
                _listState.value = UiState.Success(data)
            }
        }
    }

    fun onQueryChange(queryValue: String) {
        query.value = queryValue
    }

    fun onFilterChange(filterValue: NotificationFilter) {
        filter.value = filterValue
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            settingsRepository.markNotificationRead(id)
        }
    }

    fun filteredNotifications(data: NotificationsUiState): List<AppNotification> {
        return data.notifications.filter { notification ->
            val matchesFilter = when (data.filter) {
                NotificationFilter.ALL -> true
                NotificationFilter.ANNOUNCEMENTS ->
                    notification.category == NotificationCategory.ANNOUNCEMENT
                NotificationFilter.TIME_TABLE ->
                    notification.category == NotificationCategory.TIME_TABLE
                NotificationFilter.IMPORTANT ->
                    notification.category == NotificationCategory.IMPORTANT
            }
            val q = data.query.trim().lowercase()
            val matchesQuery = q.isBlank() ||
                notification.title.lowercase().contains(q) ||
                notification.body.lowercase().contains(q)
            matchesFilter && matchesQuery
        }
    }
}

class NotificationDetailViewModel(
    private val notificationId: String,
    getNotificationsUseCase: GetNotificationsUseCase,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _notification = MutableStateFlow<AppNotification?>(null)
    val notification: StateFlow<AppNotification?> = _notification.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.markNotificationRead(notificationId)
            getNotificationsUseCase().collect { list ->
                _notification.value = list.find { it.id == notificationId }
            }
        }
    }
}
