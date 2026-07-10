package com.studies.rrbmustudies.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.DownloadedPaper
import com.studies.rrbmustudies.domain.usecase.ObserveDownloadedPapersUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DownloadedPapersViewModel(
    observeDownloadedPapersUseCase: ObserveDownloadedPapersUseCase,
) : ViewModel() {
    val papers: StateFlow<List<DownloadedPaper>> = observeDownloadedPapersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
