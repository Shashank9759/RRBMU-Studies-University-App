package com.studies.rrbmustudies.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.ui.components.AdBannerSlot
import com.studies.rrbmustudies.ui.components.CourseListRow
import com.studies.rrbmustudies.ui.components.EmptyState
import com.studies.rrbmustudies.ui.components.FilterChip
import com.studies.rrbmustudies.ui.components.FilterChipRow
import com.studies.rrbmustudies.ui.components.PaperCard
import com.studies.rrbmustudies.ui.components.SearchBar
import com.studies.rrbmustudies.ui.state.UiState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onPaperClick: (Paper) -> Unit,
    onCourseClick: (Course) -> Unit = {},
    viewModel: SearchViewModel = koinViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val tab by viewModel.tab.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SearchBar(
                    query = query,
                    onQueryChange = viewModel::onQueryChange,
                    placeholder = "Search courses, subjects, papers…",
                    showFilterIcon = false,
                )
            }
            item {
                FilterChipRow {
                    SearchTab.entries.forEach { entry ->
                        FilterChip(
                            label = entry.label,
                            selected = tab == entry,
                            onClick = { viewModel.onTabChange(entry) },
                        )
                    }
                }
            }
            when (val uiState = state) {
                is UiState.Success -> {
                    val data = uiState.data
                    if (query.isBlank()) {
                        item {
                            EmptyState(
                                title = "Search everything",
                                message = "Find courses, subjects, and question papers in one place.",
                            )
                        }
                    } else {
                        val showCourses = tab == SearchTab.ALL || tab == SearchTab.COURSES
                        val showSubjects = tab == SearchTab.ALL || tab == SearchTab.SUBJECTS
                        val showPapers = tab == SearchTab.ALL || tab == SearchTab.PAPERS
                        val tabEmpty = when (tab) {
                            SearchTab.ALL -> data.courses.isEmpty() &&
                                data.subjects.isEmpty() &&
                                data.papers.isEmpty()
                            SearchTab.COURSES -> data.courses.isEmpty()
                            SearchTab.SUBJECTS -> data.subjects.isEmpty()
                            SearchTab.PAPERS -> data.papers.isEmpty()
                        }
                        if (data.isSearching && tabEmpty) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else if (tabEmpty) {
                            item {
                                EmptyState(
                                    title = "No results",
                                    message = "Nothing matched \"$query\".",
                                )
                            }
                        } else {
                            if (showCourses && data.courses.isNotEmpty()) {
                                item { SectionLabel("Courses") }
                                items(data.courses, key = { it.id }) { course ->
                                    CourseListRow(
                                        course = course,
                                        onClick = { onCourseClick(course) },
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                    )
                                }
                            }
                            if (showSubjects && data.subjects.isNotEmpty()) {
                                item { SectionLabel("Subjects") }
                                items(data.subjects, key = { it.subject }) { hit ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.onQueryChange(hit.subject)
                                                viewModel.onTabChange(SearchTab.PAPERS)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                    ) {
                                        Text(
                                            text = hit.subject,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            text = "${hit.paperCount} paper${if (hit.paperCount == 1) "" else "s"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                            if (showPapers && data.papers.isNotEmpty()) {
                                item { SectionLabel("Papers") }
                                items(data.papers, key = { it.id }) { paper ->
                                    PaperCard(
                                        paper = paper,
                                        onClick = { onPaperClick(paper) },
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> item {
                    com.studies.rrbmustudies.ui.components.ErrorState(
                        message = uiState.message,
                        onRetry = uiState.retry ?: { viewModel.onQueryChange(query) },
                    )
                }
                is UiState.Loading -> Unit
            }
            item { AdBannerSlot() }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
