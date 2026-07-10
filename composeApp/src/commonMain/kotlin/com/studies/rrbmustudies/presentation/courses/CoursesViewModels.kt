package com.studies.rrbmustudies.presentation.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.CourseLevel
import com.studies.rrbmustudies.domain.model.CourseSystem
import com.studies.rrbmustudies.domain.model.Part
import com.studies.rrbmustudies.domain.model.SystemType
import com.studies.rrbmustudies.domain.usecase.GetCourseSystemsUseCase
import com.studies.rrbmustudies.domain.usecase.GetCourseUseCase
import com.studies.rrbmustudies.domain.usecase.GetCoursesUseCase
import com.studies.rrbmustudies.domain.usecase.GetPapersUseCase
import com.studies.rrbmustudies.domain.usecase.GetPartsUseCase
import com.studies.rrbmustudies.ui.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class CoursesUiState(
    val courses: List<Course> = emptyList(),
    val searchQuery: String = "",
    val selectedLevel: CourseLevel? = null,
) {
    val filteredCourses: List<Course>
        get() = courses.filter { course ->
            (selectedLevel == null || course.level == selectedLevel) &&
                (searchQuery.isBlank() ||
                    course.name.contains(searchQuery, ignoreCase = true) ||
                    course.shortName.contains(searchQuery, ignoreCase = true))
        }
}

class CoursesViewModel(
    private val getCoursesUseCase: GetCoursesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<CoursesUiState>>(UiState.Loading)
    val state: StateFlow<UiState<CoursesUiState>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getCoursesUseCase()
                .catch {
                    _state.value = UiState.Error("Couldn't load courses. Pull to refresh.") { refresh() }
                }
                .collect { courses ->
                    val current = (_state.value as? UiState.Success)?.data
                    _state.value = UiState.Success(
                        CoursesUiState(
                            courses = courses,
                            searchQuery = current?.searchQuery.orEmpty(),
                            selectedLevel = current?.selectedLevel,
                        ),
                    )
                }
        }
    }

    fun refresh() {
        if (_state.value is UiState.Error) _state.value = UiState.Loading
    }

    fun onSearchQueryChange(query: String) {
        val current = (_state.value as? UiState.Success)?.data ?: return
        _state.value = UiState.Success(current.copy(searchQuery = query))
    }

    fun onLevelFilter(level: CourseLevel?) {
        val current = (_state.value as? UiState.Success)?.data ?: return
        _state.value = UiState.Success(current.copy(selectedLevel = level))
    }
}

data class CourseDetailUiState(
    val courseName: String = "",
    val backgroundImageUrl: String? = null,
    val systems: List<CourseSystem> = emptyList(),
    val selectedSystemType: SystemType = SystemType.YEARLY,
    val parts: List<Part> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class CourseDetailViewModel(
    private val getCourseSystemsUseCase: GetCourseSystemsUseCase,
    private val getPartsUseCase: GetPartsUseCase,
    private val getCourseUseCase: GetCourseUseCase,
    private val courseId: String,
    courseName: String,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<CourseDetailUiState>>(UiState.Loading)
    val state: StateFlow<UiState<CourseDetailUiState>> = _state.asStateFlow()

    private val selectedSystemType = MutableStateFlow(SystemType.YEARLY)

    init {
        load(courseName)
    }

    private fun load(courseName: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            combine(
                getCourseUseCase(courseId).catch { emit(null) },
                getCourseSystemsUseCase(courseId)
                    .flatMapLatest { systems ->
                        if (systems.isNotEmpty() && systems.none { it.type == selectedSystemType.value }) {
                            selectedSystemType.value = systems.first().type
                        }
                        selectedSystemType.map { type -> systems to type }
                    }
                    .flatMapLatest { (systems, type) ->
                        val system = systems.firstOrNull { it.type == type } ?: systems.firstOrNull()
                        if (system == null) {
                            kotlinx.coroutines.flow.flowOf(
                                CourseDetailUiState(
                                    courseName = courseName,
                                    systems = systems,
                                    selectedSystemType = type,
                                    parts = emptyList(),
                                ),
                            )
                        } else {
                            getPartsUseCase(courseId, system.id).map { parts ->
                                CourseDetailUiState(
                                    courseName = courseName,
                                    systems = systems,
                                    selectedSystemType = system.type,
                                    parts = parts,
                                )
                            }
                        }
                    },
            ) { course, detail ->
                detail.copy(
                    courseName = course?.name?.takeIf { it.isNotBlank() } ?: courseName,
                    backgroundImageUrl = course?.backgroundImageUrl,
                )
            }
                .catch {
                    _state.value = UiState.Error("Couldn't load course. Pull to refresh.") { load(courseName) }
                }
                .collect { data ->
                    _state.value = UiState.Success(data)
                }
        }
    }

    fun onSystemTypeSelected(type: SystemType) {
        selectedSystemType.value = type
    }

    fun refresh() {
        val name = (_state.value as? UiState.Success)?.data?.courseName.orEmpty()
        load(name)
    }
}

data class PartPapersUiState(
    val partName: String = "",
    val papers: List<com.studies.rrbmustudies.domain.model.Paper> = emptyList(),
    val subjectFilter: String = "",
    val yearFilter: Int? = null,
    val availableSubjects: List<String> = emptyList(),
    val availableYears: List<Int> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class PartPapersViewModel(
    private val getPapersUseCase: GetPapersUseCase,
    courseId: String,
    systemId: String,
    partId: String,
    partName: String,
    private val isAdmin: Boolean = false,
) : ViewModel() {

    private val courseId = courseId
    private val systemId = systemId
    private val partId = partId

    private val subjectFilter = MutableStateFlow("")
    private val yearFilter = MutableStateFlow<Int?>(null)

    private val _state = MutableStateFlow<UiState<PartPapersUiState>>(UiState.Loading)
    val state: StateFlow<UiState<PartPapersUiState>> = _state.asStateFlow()

    init {
        load(partName)
    }

    private fun load(partName: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            // Load full list once so year/subject chips stay stable; filter client-side.
            getPapersUseCase(
                courseId = courseId,
                systemId = systemId,
                partId = partId,
                subjectFilter = null,
                yearFilter = null,
                includeUnpublished = isAdmin,
            ).flatMapLatest { allPapers ->
                subjectFilter.flatMapLatest { subject ->
                    yearFilter.map { year ->
                        val filtered = allPapers
                            .filter { year == null || it.year == year }
                            .filter { subject.isBlank() || it.subject.equals(subject, ignoreCase = true) }
                            .sortedWith(
                                compareByDescending<com.studies.rrbmustudies.domain.model.Paper> { it.year }
                                    .thenByDescending { it.createdAt }
                                    .thenBy { it.title },
                            )
                        PartPapersUiState(
                            partName = partName,
                            papers = filtered,
                            subjectFilter = subject,
                            yearFilter = year,
                            availableSubjects = allPapers.map { it.subject }
                                .filter { it.isNotBlank() }
                                .distinct()
                                .sorted(),
                            availableYears = allPapers.map { it.year }.distinct().sortedDescending(),
                        )
                    }
                }
            }.catch {
                _state.value = UiState.Error("Couldn't load papers. Pull to refresh.") { load(partName) }
            }.collect { data ->
                _state.value = UiState.Success(data)
            }
        }
    }

    fun onSubjectFilterChange(subject: String) {
        subjectFilter.value = subject
    }

    fun onYearFilterChange(year: Int?) {
        yearFilter.value = year
    }

    fun refresh() {
        val name = (_state.value as? UiState.Success)?.data?.partName.orEmpty()
        load(name)
    }
}
