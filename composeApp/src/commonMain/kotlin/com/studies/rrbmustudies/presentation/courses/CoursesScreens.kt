package com.studies.rrbmustudies.presentation.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.CourseLevel
import com.studies.rrbmustudies.domain.model.SystemType
import com.studies.rrbmustudies.ads.NativeAdCard
import com.studies.rrbmustudies.ui.components.AdBannerSlot
import com.studies.rrbmustudies.ui.components.AdminFab
import com.studies.rrbmustudies.ui.components.CourseListRow
import com.studies.rrbmustudies.ui.components.CrestWatermark
import com.studies.rrbmustudies.ui.components.EmptyState
import com.studies.rrbmustudies.ui.components.ErrorState
import com.studies.rrbmustudies.ui.components.FilterChip
import com.studies.rrbmustudies.ui.components.FilterChipPrimaryContainer
import com.studies.rrbmustudies.ui.components.FilterChipRow
import com.studies.rrbmustudies.ui.components.LevelSectionHeader
import com.studies.rrbmustudies.ui.components.LoadingShimmer
import com.studies.rrbmustudies.ui.components.PartExploreCard
import com.studies.rrbmustudies.ui.components.PaperCard
import com.studies.rrbmustudies.ui.components.RrbmuTopBar
import com.studies.rrbmustudies.ui.components.SearchBar
import com.studies.rrbmustudies.ui.components.StitchBreadcrumb
import com.studies.rrbmustudies.ui.state.UiState
import com.studies.rrbmustudies.ui.theme.Saffron
import com.studies.rrbmustudies.ui.theme.SaffronBrush
import com.studies.rrbmustudies.ui.theme.courseHeroBrush
import com.studies.rrbmustudies.ui.theme.courseJewel
import com.studies.rrbmustudies.ui.theme.levelCategoryLabel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CoursesScreen(
    onCourseClick: (Course) -> Unit,
    viewModel: CoursesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing = state is UiState.Loading
    val pullRefreshState = rememberPullRefreshState(isRefreshing, viewModel::refresh)

    when (val uiState = state) {
        is UiState.Loading -> LoadingShimmer()
        is UiState.Error -> ErrorState(
            message = uiState.message,
            onRetry = uiState.retry ?: viewModel::refresh,
        )
        is UiState.Success -> {
            val data = uiState.data
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    item {
                        SearchBar(
                            query = data.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            placeholder = "Search courses…",
                            showFilterIcon = false,
                        )
                    }
                    item {
                        FilterChipRow {
                            FilterChip(
                                label = "All",
                                selected = data.selectedLevel == null,
                                onClick = { viewModel.onLevelFilter(null) },
                            )
                            FilterChip(
                                label = "UG",
                                selected = data.selectedLevel == CourseLevel.UG,
                                onClick = { viewModel.onLevelFilter(CourseLevel.UG) },
                            )
                            FilterChip(
                                label = "PG",
                                selected = data.selectedLevel == CourseLevel.PG,
                                onClick = { viewModel.onLevelFilter(CourseLevel.PG) },
                            )
                            FilterChip(
                                label = "Diploma",
                                selected = data.selectedLevel == CourseLevel.DIPLOMA,
                                onClick = { viewModel.onLevelFilter(CourseLevel.DIPLOMA) },
                            )
                        }
                    }
                    if (data.filteredCourses.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No courses found",
                                message = if (data.courses.isEmpty()) {
                                    "Courses will appear here once published in Firebase."
                                } else {
                                    "Try adjusting your search or filter."
                                },
                            )
                        }
                    } else {
                        val grouped = data.filteredCourses.groupBy { it.level }
                        listOf(CourseLevel.UG, CourseLevel.PG, CourseLevel.DIPLOMA).forEach { level ->
                            val courses = grouped[level].orEmpty()
                            if (courses.isNotEmpty()) {
                                item { LevelSectionHeader(title = levelCategoryLabel(level)) }
                                items(courses) { course ->
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    )
                                    CourseListRow(
                                        course = course,
                                        onClick = { onCourseClick(course) },
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                    item { AdBannerSlot() }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseDetailScreen(
    courseShortName: String,
    onBack: () -> Unit,
    onPartClick: (String, String, String, SystemType) -> Unit,
    viewModel: CourseDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing = state is UiState.Loading
    val pullRefreshState = rememberPullRefreshState(isRefreshing, viewModel::refresh)

    Scaffold(
        topBar = {
            RrbmuTopBar(
                showBack = true,
                onBack = onBack,
            )
        },
    ) { padding ->
        when (val uiState = state) {
            is UiState.Loading -> LoadingShimmer(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(
                message = uiState.message,
                onRetry = uiState.retry ?: viewModel::refresh,
                modifier = Modifier.padding(padding),
            )
            is UiState.Success -> {
                val data = uiState.data
                val totalPapers = data.parts.sumOf { it.paperCount }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn {
                        item {
                            CourseHeroHeader(
                                courseShortName = courseShortName,
                                title = buildCourseHeroTitle(courseShortName, data.courseName),
                                paperCount = totalPapers,
                                backgroundImageUrl = data.backgroundImageUrl,
                                onBack = onBack,
                            )
                        }
                        item {
                            SystemTabRow(
                                systems = data.systems,
                                selected = data.selectedSystemType,
                                onSelected = viewModel::onSystemTypeSelected,
                            )
                        }
                        item { AdBannerSlot() }
                        if (data.parts.isEmpty()) {
                            item {
                                EmptyState(
                                    title = "No sections yet",
                                    message = "This course has no systems or parts yet. Ask an admin to seed the course.",
                                )
                            }
                        } else {
                            items(data.parts.size) { index ->
                                val part = data.parts[index]
                                val system = data.systems.firstOrNull { it.type == data.selectedSystemType }
                                PartExploreCard(
                                    partName = part.name,
                                    description = part.description.orEmpty(),
                                    paperCount = part.paperCount,
                                    paperCountLabel = "Papers & Study Notes",
                                    highlighted = false,
                                    partNumber = index + 1,
                                    onExplore = {
                                        system?.let {
                                            onPartClick(it.id, part.id, part.name, data.selectedSystemType)
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
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
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PartPapersScreen(
    courseShortName: String,
    systemLabel: String,
    onBack: () -> Unit,
    onPaperClick: (String) -> Unit,
    isAdmin: Boolean = false,
    onUploadClick: () -> Unit = {},
    viewModel: PartPapersViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing = state is UiState.Loading
    val pullRefreshState = rememberPullRefreshState(isRefreshing, viewModel::refresh)
    var yearMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RrbmuTopBar(
                showBack = true,
                onBack = onBack,
            )
        },
        floatingActionButton = {
            if (isAdmin) AdminFab(onClick = onUploadClick, contentDescription = "Upload paper")
        },
    ) { padding ->
        when (val uiState = state) {
            is UiState.Loading -> LoadingShimmer(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(
                message = uiState.message,
                onRetry = uiState.retry ?: viewModel::refresh,
                modifier = Modifier.padding(padding),
            )
            is UiState.Success -> {
                val data = uiState.data
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                                StitchBreadcrumb(
                                    segments = listOf(
                                        courseShortName.ifBlank { "Course" },
                                        systemLabel,
                                        data.partName,
                                    ),
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Available Papers",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                                .clickableMenu { yearMenuExpanded = true }
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = data.yearFilter?.toString() ?: "All",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                            androidx.compose.material3.Icon(
                                                Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = yearMenuExpanded,
                                            onDismissRequest = { yearMenuExpanded = false },
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("All") },
                                                onClick = {
                                                    viewModel.onYearFilterChange(null)
                                                    yearMenuExpanded = false
                                                },
                                            )
                                            data.availableYears.forEach { year ->
                                                DropdownMenuItem(
                                                    text = { Text(year.toString()) },
                                                    onClick = {
                                                        viewModel.onYearFilterChange(year)
                                                        yearMenuExpanded = false
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (data.availableSubjects.isNotEmpty()) {
                            item {
                                FilterChipRow {
                                    FilterChipPrimaryContainer(
                                        label = "All Subjects",
                                        selected = data.subjectFilter.isBlank(),
                                        onClick = { viewModel.onSubjectFilterChange("") },
                                    )
                                    data.availableSubjects.forEach { subject ->
                                        FilterChipPrimaryContainer(
                                            label = subject,
                                            selected = data.subjectFilter == subject,
                                            onClick = { viewModel.onSubjectFilterChange(subject) },
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        item { AdBannerSlot() }
                        if (data.papers.isEmpty()) {
                            item {
                                EmptyState(
                                    title = "No papers yet",
                                    message = "This section is empty. Check back later or browse other parts.",
                                )
                            }
                        } else {
                            itemsIndexed(data.papers) { index, paper ->
                                PaperCard(
                                    paper = paper,
                                    onClick = { onPaperClick(paper.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                )
                                if (index == 3) {
                                    NativeAdCard()
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
}

@Composable
private fun CourseHeroHeader(
    courseShortName: String,
    title: String,
    paperCount: Int,
    onBack: () -> Unit,
    backgroundImageUrl: String? = null,
) {
    val jewel = courseJewel(courseShortName)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(courseHeroBrush(jewel)),
    ) {
        if (!backgroundImageUrl.isNullOrBlank()) {
            coil3.compose.AsyncImage(
                model = backgroundImageUrl,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
            // Indigo scrim keeps the white text readable over any photo.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x660A1046),
                                Color(0xCC0A1046),
                            ),
                        ),
                    ),
            )
        }
        CrestWatermark(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 8.dp)
                .size(160.dp)
                .graphicsLayer { alpha = 0.10f },
        )
        // Frosted back pill (top-left)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 16.dp, start = 20.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .clickableMenu(onBack)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            androidx.compose.material3.Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Back to Courses",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = " RRBMU • Alwar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
                if (paperCount > 0) {
                    Text(
                        text = "$paperCount Papers",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.20f))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemTabRow(
    systems: List<com.studies.rrbmustudies.domain.model.CourseSystem>,
    selected: SystemType,
    onSelected: (SystemType) -> Unit,
) {
    val tabs = systems
        .map { it.type to systemTabLabel(it.type) }
        .distinctBy { it.first }
        // Firestore returns systems in doc-id (alphabetical) order; force the intended
        // Yearly → Semester → Entrance order regardless of how they come back.
        .sortedBy { it.first.ordinal }
        .ifEmpty {
            listOf(
                SystemType.YEARLY to systemTabLabel(SystemType.YEARLY),
                SystemType.SEMESTER to systemTabLabel(SystemType.SEMESTER),
                SystemType.ENTRANCE to systemTabLabel(SystemType.ENTRANCE),
            )
        }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp),
    ) {
        tabs.forEach { (type, label) ->
            val active = selected == type
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickableMenu { onSelected(type) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (active) Saffron
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) SaffronBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))),
                )
            }
        }
    }
}

private fun systemTabLabel(type: SystemType): String = when (type) {
    SystemType.YEARLY -> "Yearly Wise"
    SystemType.SEMESTER -> "Semester Wise"
    SystemType.ENTRANCE -> "Entrance Exam"
}

private fun buildCourseHeroTitle(shortName: String, fullName: String): String {
    val short = shortName.ifBlank { fullName.substringBefore(" -").trim() }
    val name = fullName.ifBlank { shortName }
    return if (name.contains(short, ignoreCase = true)) name else "$short - $name"
}

private fun Modifier.clickableMenu(onClick: () -> Unit): Modifier = clickable(onClick = onClick)
