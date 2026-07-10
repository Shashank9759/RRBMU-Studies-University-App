package com.studies.rrbmustudies.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.AdminUser
import com.studies.rrbmustudies.domain.model.ThemeMode
import com.studies.rrbmustudies.domain.usecase.ClearPdfVaultUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveAdminStateUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveThemeModeUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveVaultStorageUseCase
import com.studies.rrbmustudies.domain.usecase.SetThemeModeUseCase
import com.studies.rrbmustudies.domain.usecase.SignOutAdminUseCase
import com.studies.rrbmustudies.ui.util.toUserMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val pushNotificationsEnabled: Boolean = true,
    val downloadedPapersCount: Int = 0,
    val downloadedPapersSizeMb: String = "0 MB",
    val isAdmin: Boolean = false,
    val isAdminResolved: Boolean = false,
    val adminUser: AdminUser? = null,
    val showAdminLoginSheet: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showClearDownloadsDialog: Boolean = false,
    val isClearingDownloads: Boolean = false,
)

sealed interface SettingsEvent {
    data class Snackbar(val message: String) : SettingsEvent
}

class SettingsViewModel(
    observeThemeModeUseCase: ObserveThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    observeAdminStateUseCase: ObserveAdminStateUseCase,
    private val signOutAdminUseCase: SignOutAdminUseCase,
    observeVaultStorageUseCase: ObserveVaultStorageUseCase,
    private val clearPdfVaultUseCase: ClearPdfVaultUseCase,
) : ViewModel() {

    private val localState = MutableStateFlow(SettingsUiState())

    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        observeThemeModeUseCase(),
        observeAdminStateUseCase.isAdmin,
        observeAdminStateUseCase.currentUser,
        observeVaultStorageUseCase(),
        localState,
    ) { themeMode, isAdmin, adminUser, vault, local ->
        local.copy(
            themeMode = themeMode,
            isAdmin = isAdmin,
            isAdminResolved = true,
            adminUser = adminUser,
            downloadedPapersCount = vault.count,
            downloadedPapersSizeMb = vault.sizeLabel,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { setThemeModeUseCase(mode) }
    }

    fun setPushNotifications(enabled: Boolean) {
        localState.update { it.copy(pushNotificationsEnabled = enabled) }
    }

    fun showAdminLogin() {
        localState.update { it.copy(showAdminLoginSheet = true) }
    }

    fun dismissAdminLogin() {
        localState.update { it.copy(showAdminLoginSheet = false) }
    }

    fun onAdminLoginSuccess() {
        localState.update { it.copy(showAdminLoginSheet = false) }
    }

    fun showLogoutDialog() {
        localState.update { it.copy(showLogoutDialog = true) }
    }

    fun dismissLogoutDialog() {
        localState.update { it.copy(showLogoutDialog = false) }
    }

    fun confirmLogout() {
        viewModelScope.launch {
            signOutAdminUseCase()
            localState.update { it.copy(showLogoutDialog = false) }
        }
    }

    fun showClearDownloadsDialog() {
        localState.update { it.copy(showClearDownloadsDialog = true) }
    }

    fun dismissClearDownloadsDialog() {
        localState.update { it.copy(showClearDownloadsDialog = false) }
    }

    fun clearDownloadedPapers() {
        viewModelScope.launch {
            localState.update { it.copy(isClearingDownloads = true) }
            val result = clearPdfVaultUseCase()
            localState.update {
                it.copy(isClearingDownloads = false, showClearDownloadsDialog = false)
            }
            if (result.isSuccess) {
                _events.emit(SettingsEvent.Snackbar("Downloaded papers deleted."))
            } else {
                _events.emit(
                    SettingsEvent.Snackbar(
                        result.exceptionOrNull()?.toUserMessage("Couldn't delete downloads")
                            ?: "Couldn't delete downloads",
                    ),
                )
            }
        }
    }
}

// Theme-only accessor for App root — keeps theme persistence outside Settings screen.
class AppSettingsViewModel(
    observeThemeModeUseCase: ObserveThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = observeThemeModeUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { setThemeModeUseCase(mode) }
    }
}
