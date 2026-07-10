package com.studies.rrbmustudies.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.AppNotification
import com.studies.rrbmustudies.domain.usecase.CreateNotificationUseCase
import com.studies.rrbmustudies.domain.usecase.DeleteNotificationUseCase
import com.studies.rrbmustudies.domain.usecase.GetAllNotificationsUseCase
import com.studies.rrbmustudies.domain.usecase.UpdateNotificationUseCase
import com.studies.rrbmustudies.domain.usecase.UploadNotificationAttachmentUseCase
import com.studies.rrbmustudies.ui.state.UiState
import com.studies.rrbmustudies.ui.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ManageNotificationsViewModel(
    private val getAllNotificationsUseCase: GetAllNotificationsUseCase,
    private val createNotificationUseCase: CreateNotificationUseCase,
    private val updateNotificationUseCase: UpdateNotificationUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    private val uploadNotificationAttachmentUseCase: UploadNotificationAttachmentUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<AppNotification>>>(UiState.Loading)
    val state: StateFlow<UiState<List<AppNotification>>> = _state.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isUploadingAttachment = MutableStateFlow(false)
    val isUploadingAttachment: StateFlow<Boolean> = _isUploadingAttachment.asStateFlow()

    private val _uploadedAttachmentUrl = MutableStateFlow<String?>(null)
    val uploadedAttachmentUrl: StateFlow<String?> = _uploadedAttachmentUrl.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            getAllNotificationsUseCase()
                .catch { e ->
                    _state.value = UiState.Error(
                        e.toUserMessage("Couldn't load notifications. Pull to refresh."),
                    ) { refresh() }
                }
                .collect { list -> _state.value = UiState.Success(list) }
        }
    }

    fun uploadAttachment(bytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _isUploadingAttachment.value = true
            uploadNotificationAttachmentUseCase(bytes, fileName).fold(
                onSuccess = { url ->
                    _uploadedAttachmentUrl.value = url
                    _actionMessage.value = "File attached"
                },
                onFailure = { e ->
                    _actionMessage.value = e.toUserMessage("Couldn't upload file. Try again.")
                },
            )
            _isUploadingAttachment.value = false
        }
    }

    fun clearUploadedAttachment() {
        _uploadedAttachmentUrl.value = null
    }

    fun save(notification: AppNotification) {
        viewModelScope.launch {
            if (notification.title.isBlank() || notification.body.isBlank()) {
                _actionMessage.value = "Title and body are required."
                return@launch
            }
            val normalized = notification.copy(
                title = notification.title.trim(),
                body = notification.body.trim(),
                attachmentUrl = notification.attachmentUrl?.trim()?.takeIf { it.isNotBlank() },
                linkUrl = notification.linkUrl?.trim()?.takeIf { it.isNotBlank() }
                    ?: notification.attachmentUrl?.trim()?.takeIf { it.isNotBlank() },
            )
            val result = if (normalized.id.isBlank()) {
                createNotificationUseCase(normalized)
            } else {
                updateNotificationUseCase(normalized)
            }
            result.fold(
                onSuccess = {
                    _actionMessage.value = "Notification saved"
                    _uploadedAttachmentUrl.value = null
                    refresh()
                },
                onFailure = { e ->
                    _actionMessage.value = e.toUserMessage("Couldn't save notification. Try again.")
                },
            )
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            deleteNotificationUseCase(id).fold(
                onSuccess = { _actionMessage.value = "Deleted"; refresh() },
                onFailure = { e ->
                    _actionMessage.value = e.toUserMessage("Couldn't delete notification. Try again.")
                },
            )
        }
    }

    fun clearMessage() { _actionMessage.value = null }
}
