package com.studies.rrbmustudies.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.domain.usecase.GetCoursesUseCase
import com.studies.rrbmustudies.domain.usecase.LogSearchUseCase
import com.studies.rrbmustudies.domain.usecase.SearchPapersUseCase
import com.studies.rrbmustudies.ui.state.UiState
import com.studies.rrbmustudies.ui.util.toUserMessage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

enum class SearchTab(val label: String) {
    ALL("All"),
    COURSES("Courses"),
    SUBJECTS("Subjects"),
    PAPERS("Papers"),
}

data class SubjectSearchHit(
    val subject: String,
    val paperCount: Int,
)

data class SearchUiData(
    val tab: SearchTab = SearchTab.ALL,
    val courses: List<Course> = emptyList(),
    val subjects: List<SubjectSearchHit> = emptyList(),
    val papers: List<Paper> = emptyList(),
    val isSearching: Boolean = false,
)

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val searchPapersUseCase: SearchPapersUseCase,
    private val getCoursesUseCase: GetCoursesUseCase,
    private val logSearchUseCase: LogSearchUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _tab = MutableStateFlow(SearchTab.ALL)
    val tab: StateFlow<SearchTab> = _tab.asStateFlow()

    private val _state = MutableStateFlow<UiState<SearchUiData>>(UiState.Success(SearchUiData()))
    val state: StateFlow<UiState<SearchUiData>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_query.debounce(300).distinctUntilChanged(), _tab) { q, tab -> q to tab }
                .flatMapLatest { (q, tab) ->
                    if (q.isBlank()) {
                        flowOf(SearchUiData(tab = tab, isSearching = false))
                    } else {
                        val needle = q.trim()
                        combine(
                            getCoursesUseCase().catch { emit(emptyList()) },
                            // No onStart(empty): let combine wait for real results so the list
                            // doesn't flash empty. The outer onStart keeps prior results visible.
                            searchPapersUseCase(needle)
                                .catch { emit(emptyList()) },
                        ) { courses, papers ->
                            val courseHits = courses.filter {
                                it.name.contains(needle, ignoreCase = true) ||
                                    it.shortName.contains(needle, ignoreCase = true)
                            }
                            val paperHits = papers
                            val subjectHits = paperHits
                                .groupBy { it.subject.trim() }
                                .filterKeys { it.isNotBlank() }
                                .map { (subject, list) -> SubjectSearchHit(subject, list.size) }
                                .sortedByDescending { it.paperCount }
                            SearchUiData(
                                tab = tab,
                                courses = courseHits,
                                subjects = subjectHits,
                                papers = paperHits,
                                isSearching = false,
                            )
                        }.onStart {
                            val previous = (_state.value as? UiState.Success)?.data
                            emit(
                                previous?.copy(tab = tab, isSearching = true)
                                    ?: SearchUiData(tab = tab, isSearching = true),
                            )
                        }
                    }
                }
                .distinctUntilChanged()
                .catch { e ->
                    _state.value = UiState.Error(
                        e.toUserMessage("Couldn't search. Try again."),
                    ) { onQueryChange(_query.value) }
                }
                .collect { data ->
                    _state.value = UiState.Success(data)
                    val query = _query.value
                    if (query.isNotBlank() && !data.isSearching) {
                        val total = data.courses.size + data.subjects.size + data.papers.size
                        logSearchUseCase(query, total)
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onTabChange(tab: SearchTab) {
        _tab.value = tab
        val current = (_state.value as? UiState.Success)?.data
        if (current != null) {
            _state.value = UiState.Success(current.copy(tab = tab))
        }
    }
}
