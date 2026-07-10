package com.studies.rrbmustudies.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.usecase.CreateCourseUseCase
import com.studies.rrbmustudies.domain.usecase.GetAllCoursesUseCase
import com.studies.rrbmustudies.domain.usecase.UpdateCourseUseCase
import com.studies.rrbmustudies.domain.usecase.UploadCourseBackgroundUseCase
import com.studies.rrbmustudies.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ManageCoursesViewModel(
    private val getAllCoursesUseCase: GetAllCoursesUseCase,
    private val createCourseUseCase: CreateCourseUseCase,
    private val updateCourseUseCase: UpdateCourseUseCase,
    private val uploadCourseBackgroundUseCase: UploadCourseBackgroundUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Course>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Course>>> = _state.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isUploadingBackground = MutableStateFlow(false)
    val isUploadingBackground: StateFlow<Boolean> = _isUploadingBackground.asStateFlow()

    private val _uploadedBackgroundUrl = MutableStateFlow<String?>(null)
    val uploadedBackgroundUrl: StateFlow<String?> = _uploadedBackgroundUrl.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            getAllCoursesUseCase()
                .catch {
                    _state.value = UiState.Error("Couldn't load courses. Pull to refresh.") { refresh() }
                }
                .collect { courses ->
                    _state.value = UiState.Success(courses)
                }
        }
    }

    fun uploadBackground(bytes: ByteArray, fileName: String, courseId: String) {
        viewModelScope.launch {
            _isUploadingBackground.value = true
            uploadCourseBackgroundUseCase(bytes, fileName, courseId).fold(
                onSuccess = { url ->
                    _uploadedBackgroundUrl.value = url
                    _actionMessage.value = "Background uploaded"
                },
                onFailure = {
                    _actionMessage.value = "Couldn't upload background. Try again."
                },
            )
            _isUploadingBackground.value = false
        }
    }

    fun clearUploadedBackground() {
        _uploadedBackgroundUrl.value = null
    }

    fun saveCourse(course: Course, isNew: Boolean) {
        viewModelScope.launch {
            val normalized = course.copy(
                id = course.id.trim().lowercase().ifBlank {
                    course.shortName.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                },
                backgroundImageUrl = course.backgroundImageUrl?.trim()?.ifBlank { null }
                    ?: _uploadedBackgroundUrl.value,
            )
            val result = if (isNew) createCourseUseCase(normalized) else updateCourseUseCase(normalized)
            result.fold(
                onSuccess = {
                    _actionMessage.value = if (isNew) "Course created with systems and parts" else "Course updated"
                    clearUploadedBackground()
                    refresh()
                },
                onFailure = { _actionMessage.value = "Couldn't save course. Try again." },
            )
        }
    }

    fun clearMessage() { _actionMessage.value = null }
}
