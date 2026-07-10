package com.studies.rrbmustudies.presentation.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.ui.components.AdBannerSlot
import com.studies.rrbmustudies.ui.components.AdCarousel
import com.studies.rrbmustudies.ui.components.EmptyState
import com.studies.rrbmustudies.ui.components.ErrorState
import com.studies.rrbmustudies.ui.components.LoadingShimmer
import com.studies.rrbmustudies.ui.components.QuickAccessCourseCard
import com.studies.rrbmustudies.ui.components.RecentPaperCard
import com.studies.rrbmustudies.ui.components.SearchBar
import com.studies.rrbmustudies.ui.components.SectionHeader
import com.studies.rrbmustudies.ui.state.UiState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onCourseClick: (Course) -> Unit,
    onPaperClick: (Paper) -> Unit,
    onSearchClick: () -> Unit,
    onAdClick: (HomeAd) -> Unit,
    onSeeAllCourses: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing = state is UiState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = viewModel::refresh,
    )

    when (val uiState = state) {
        is UiState.Loading -> LoadingShimmer()
        is UiState.Error -> ErrorState(
            message = "Couldn't load home content. Pull to refresh.",
            onRetry = uiState.retry ?: viewModel::refresh,
        )
        is UiState.Success -> {
            val data = uiState.data
            val courseRows = data.courses.chunked(2)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item {
                        SearchBar(
                            query = "",
                            onQueryChange = {},
                            placeholder = "Search papers, subjects, courses…",
                            readOnly = true,
                            onClick = onSearchClick,
                            showFilterIcon = false,
                        )
                    }
                    if (data.ads.isNotEmpty()) {
                        item {
                            AdCarousel(
                                ads = data.ads,
                                onAdClick = onAdClick,
                                autoScrollIntervalMs = data.carouselSettings.slideIntervalSeconds * 1000L,
                            )
                        }
                    }
                    item { AdBannerSlot() }
                    item {
                        SectionHeader(
                            title = "Select Course",
                            actionLabel = "View All",
                            onActionClick = onSeeAllCourses,
                        )
                    }
                    item {
                        if (data.courses.isEmpty()) {
                            EmptyState(
                                title = "No active courses yet",
                                message = "Courses will appear here once published.",
                            )
                        } else {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                courseRows.forEach { rowCourses ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        rowCourses.forEach { course ->
                                            QuickAccessCourseCard(
                                                course = course,
                                                onClick = { onCourseClick(course) },
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                        if (rowCourses.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        SectionHeader(
                            title = "Recently Added",
                            actionLabel = if (data.recentPapers.isNotEmpty()) "View More" else null,
                            onActionClick = null,
                        )
                    }
                    if (data.recentPapers.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No recent papers yet",
                                message = "Published papers will show here once uploaded.",
                            )
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                data.recentPapers.forEach { paper ->
                                    RecentPaperCard(
                                        paper = paper,
                                        onClick = { onPaperClick(paper) },
                                    )
                                }
                            }
                        }
                    }
                }
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}
