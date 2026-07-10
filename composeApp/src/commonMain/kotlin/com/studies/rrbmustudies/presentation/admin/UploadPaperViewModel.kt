package com.studies.rrbmustudies.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.CourseSystem
import com.studies.rrbmustudies.domain.model.Part
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.domain.usecase.GetCourseSystemsUseCase
import com.studies.rrbmustudies.domain.usecase.GetCoursesUseCase
import com.studies.rrbmustudies.domain.usecase.GetPartsUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveAdminStateUseCase
import com.studies.rrbmustudies.domain.usecase.UploadPaperUseCase
import com.studies.rrbmustudies.ui.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UploadPaperUiState(
    val title: String = "",
    val subject: String = "",
    val paperCode: String = "",
    val year: String = "",
    val description: String = "",
    val isPublished: Boolean = true,
    val pickedFile: PickedFile? = null,
    val isUploading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val courseId: String = "",
    val systemId: String = "",
    val partId: String = "",
    val courses: List<Course> = emptyList(),
    val systems: List<CourseSystem> = emptyList(),
    val parts: List<Part> = emptyList(),
) {
    val selectedCourse: Course? get() = courses.find { it.id == courseId }
    val selectedSystem: CourseSystem? get() = systems.find { it.id == systemId }
    val selectedPart: Part? get() = parts.find { it.id == partId }
    val locationComplete: Boolean get() = courseId.isNotBlank() && systemId.isNotBlank() && partId.isNotBlank()
}

class UploadPaperViewModel(
    private val uploadPaperUseCase: UploadPaperUseCase,
    private val observeAdminStateUseCase: ObserveAdminStateUseCase,
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getCourseSystemsUseCase: GetCourseSystemsUseCase,
    private val getPartsUseCase: GetPartsUseCase,
    initialCourseId: String,
    initialSystemId: String,
    initialPartId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UploadPaperUiState(
            courseId = initialCourseId,
            systemId = initialSystemId,
            partId = initialPartId,
        ),
    )
    val state: StateFlow<UploadPaperUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getCoursesUseCase().collect { courses ->
                _state.update { it.copy(courses = courses) }
            }
        }
        if (initialCourseId.isNotBlank()) {
            loadSystems(initialCourseId, keepSelection = initialSystemId.isNotBlank())
        }
        if (initialCourseId.isNotBlank() && initialSystemId.isNotBlank()) {
            loadParts(initialCourseId, initialSystemId, keepSelection = initialPartId.isNotBlank())
        }
    }

    fun onTitleChange(v: String) { _state.update { it.copy(title = v) } }
    fun onSubjectChange(v: String) { _state.update { it.copy(subject = v) } }
    fun onPaperCodeChange(v: String) { _state.update { it.copy(paperCode = v) } }
    fun onYearChange(v: String) { _state.update { it.copy(year = v) } }
    fun onDescriptionChange(v: String) { _state.update { it.copy(description = v) } }
    fun onFilePicked(file: PickedFile) { _state.update { it.copy(pickedFile = file, error = null) } }

    fun onCourseSelected(courseId: String) {
        _state.update {
            it.copy(courseId = courseId, systemId = "", partId = "", systems = emptyList(), parts = emptyList(), error = null)
        }
        loadSystems(courseId, keepSelection = false)
    }

    fun onSystemSelected(systemId: String) {
        val courseId = _state.value.courseId
        _state.update { it.copy(systemId = systemId, partId = "", parts = emptyList(), error = null) }
        if (courseId.isNotBlank()) {
            loadParts(courseId, systemId, keepSelection = false)
        }
    }

    fun onPartSelected(partId: String) {
        _state.update { it.copy(partId = partId, error = null) }
    }

    private fun loadSystems(courseId: String, keepSelection: Boolean) {
        viewModelScope.launch {
            val systems = getCourseSystemsUseCase(courseId).first()
            _state.update { current ->
                val systemId = if (keepSelection && systems.any { it.id == current.systemId }) {
                    current.systemId
                } else {
                    ""
                }
                current.copy(systems = systems, systemId = systemId, parts = emptyList(), partId = "")
            }
        }
    }

    private fun loadParts(courseId: String, systemId: String, keepSelection: Boolean) {
        viewModelScope.launch {
            val parts = getPartsUseCase(courseId, systemId).first()
            _state.update { current ->
                val partId = if (keepSelection && parts.any { it.id == current.partId }) {
                    current.partId
                } else {
                    ""
                }
                current.copy(parts = parts, partId = partId)
            }
        }
    }

    fun saveDraft() = upload(publish = false)

    fun saveAndPublish() = upload(publish = true)

    private fun upload(publish: Boolean) {
        val current = _state.value
        if (!current.locationComplete) {
            _state.update { it.copy(error = "Select course, system, and part before uploading") }
            return
        }
        val year = current.year.toIntOrNull()
        if (current.title.isBlank() || current.subject.isBlank() || current.paperCode.isBlank() || year == null) {
            _state.update { it.copy(error = "Fill title, subject, code, and year") }
            return
        }
        val file = current.pickedFile
        if (file == null) {
            _state.update { it.copy(error = "Select a PDF file") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, isPublished = publish, error = null) }
            val adminUid = observeAdminStateUseCase.currentUser.first()?.uid
            val paper = Paper(
                id = "",
                courseId = current.courseId,
                systemId = current.systemId,
                partId = current.partId,
                title = current.title.trim(),
                subject = current.subject.trim(),
                paperCode = current.paperCode.trim(),
                year = year,
                description = current.description.trim().ifBlank { null },
                pdfUrl = "",
                isPublished = publish,
                createdBy = adminUid,
            )
            uploadPaperUseCase(paper, file.bytes).fold(
                onSuccess = { _state.value = UploadPaperUiState(isSuccess = true) },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isUploading = false,
                            error = e.toUserMessage("Couldn't upload paper. Try again."),
                        )
                    }
                },
            )
        }
    }
}
