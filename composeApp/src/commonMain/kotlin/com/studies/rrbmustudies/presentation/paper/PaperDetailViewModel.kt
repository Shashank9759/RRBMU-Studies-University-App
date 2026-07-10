package com.studies.rrbmustudies.presentation.paper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.ads.PaperAdGateway
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.domain.usecase.CacheViewedPaperUseCase
import com.studies.rrbmustudies.domain.usecase.DownloadPaperToVaultUseCase
import com.studies.rrbmustudies.domain.usecase.GetPaperUseCase
import com.studies.rrbmustudies.domain.usecase.GetVaultDownloadUseCase
import com.studies.rrbmustudies.domain.usecase.IncrementDownloadCountUseCase
import com.studies.rrbmustudies.domain.usecase.LogDownloadUseCase
import com.studies.rrbmustudies.domain.usecase.LogPaperViewUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveAdminStateUseCase
import com.studies.rrbmustudies.domain.usecase.ObservePaperDownloadedUseCase
import com.studies.rrbmustudies.ui.state.UiState
import com.studies.rrbmustudies.ui.util.toUserMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaperDetailUi(
    val paper: Paper,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadError: String? = null,
    /** Local vault path when offline copy exists — used for bottom preview. */
    val localPdfPath: String? = null,
)

sealed interface PaperDetailEvent {
    data class OpenPdf(val pathOrUrl: String, val title: String) : PaperDetailEvent
    data class Snackbar(val message: String) : PaperDetailEvent
}

class PaperDetailViewModel(
    private val getPaperUseCase: GetPaperUseCase,
    private val incrementDownloadCountUseCase: IncrementDownloadCountUseCase,
    private val cacheViewedPaperUseCase: CacheViewedPaperUseCase,
    private val logPaperViewUseCase: LogPaperViewUseCase,
    private val logDownloadUseCase: LogDownloadUseCase,
    private val downloadPaperToVaultUseCase: DownloadPaperToVaultUseCase,
    private val observePaperDownloadedUseCase: ObservePaperDownloadedUseCase,
    private val getVaultDownloadUseCase: GetVaultDownloadUseCase,
    private val observeAdminStateUseCase: ObserveAdminStateUseCase,
    private val paperAdGateway: PaperAdGateway,
    private val courseId: String,
    private val systemId: String,
    private val partId: String,
    private val paperId: String,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<PaperDetailUi>>(UiState.Loading)
    val state: StateFlow<UiState<PaperDetailUi>> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PaperDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PaperDetailEvent> = _events.asSharedFlow()

    private val _downloadTracked = MutableStateFlow(false)

    init {
        refresh()
        viewModelScope.launch {
            observePaperDownloadedUseCase(paperId).collect { downloaded ->
                val localPath = if (downloaded) getVaultDownloadUseCase(paperId)?.localPath else null
                _state.update { current ->
                    if (current is UiState.Success) {
                        current.copy(
                            data = current.data.copy(
                                isDownloaded = downloaded,
                                localPdfPath = localPath,
                            ),
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            getPaperUseCase(courseId, systemId, partId, paperId)
                .catch { e ->
                    _state.value = UiState.Error(
                        e.toUserMessage("Couldn't load paper. Pull to refresh."),
                    ) { refresh() }
                }
                .collect { paper ->
                    if (paper == null) {
                        _state.value = UiState.Error("Paper not found") { refresh() }
                    } else {
                        val existing = getVaultDownloadUseCase(paper.id)
                        val isDownloaded = existing != null
                        _state.value = UiState.Success(
                            PaperDetailUi(
                                paper = paper,
                                isDownloaded = isDownloaded,
                                localPdfPath = existing?.localPath,
                            ),
                        )
                        viewModelScope.launch {
                            cacheViewedPaperUseCase(paper)
                            logPaperViewUseCase(paper.id, paper.courseId)
                        }
                    }
                }
        }
    }

    /** Primary action: open in-app PDF viewer (local vault path if available). */
    fun openPdf() {
        viewModelScope.launch {
            val current = (_state.value as? UiState.Success)?.data ?: return@launch
            val isAdmin = observeAdminStateUseCase.isAdmin.first()
            paperAdGateway.requestBeforeOpenPaper(isAdmin) {
                viewModelScope.launch {
                    emitOpenPdf(current)
                }
            }
        }
    }

    private suspend fun emitOpenPdf(current: PaperDetailUi) {
        val title = current.paper.title
        val local = getVaultDownloadUseCase(current.paper.id)
        if (local != null) {
            _events.emit(PaperDetailEvent.OpenPdf(local.localPath, title))
            return
        }
        _events.emit(PaperDetailEvent.OpenPdf(current.paper.pdfUrl, title))
    }

    /** Download into app-private vault, then open offline. */
    fun downloadToVault() {
        viewModelScope.launch {
            val current = (_state.value as? UiState.Success)?.data ?: return@launch
            if (current.isDownloading) return@launch
            val isAdmin = observeAdminStateUseCase.isAdmin.first()
            paperAdGateway.requestBeforeOfflineDownload(isAdmin) {
                viewModelScope.launch {
                    performDownload(current)
                }
            }
        }
    }

    private suspend fun performDownload(current: PaperDetailUi) {
        _state.update {
            if (it is UiState.Success) {
                it.copy(data = it.data.copy(isDownloading = true, downloadError = null))
            } else {
                it
            }
        }

        val result = downloadPaperToVaultUseCase(current.paper)
        result.fold(
            onSuccess = { downloaded ->
                trackDownloadOnce()
                _state.update {
                    if (it is UiState.Success) {
                        it.copy(
                            data = it.data.copy(
                                isDownloading = false,
                                isDownloaded = true,
                                localPdfPath = downloaded.localPath,
                                downloadError = null,
                            ),
                        )
                    } else {
                        it
                    }
                }
                _events.emit(PaperDetailEvent.Snackbar("Saved for offline viewing"))
                _events.emit(PaperDetailEvent.OpenPdf(downloaded.localPath, downloaded.title))
            },
            onFailure = { error ->
                val msg = error.toUserMessage("Couldn't download paper. Try again.")
                _state.update {
                    if (it is UiState.Success) {
                        it.copy(
                            data = it.data.copy(
                                isDownloading = false,
                                downloadError = msg,
                            ),
                        )
                    } else {
                        it
                    }
                }
                _events.emit(PaperDetailEvent.Snackbar(msg))
            },
        )
    }

    private fun trackDownloadOnce() {
        if (_downloadTracked.value) return
        viewModelScope.launch {
            incrementDownloadCountUseCase(courseId, systemId, partId, paperId)
            logDownloadUseCase(paperId, courseId)
            _downloadTracked.value = true
        }
    }
}
