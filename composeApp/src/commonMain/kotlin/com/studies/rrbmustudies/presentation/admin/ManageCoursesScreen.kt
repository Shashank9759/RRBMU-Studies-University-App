package com.studies.rrbmustudies.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.CourseLevel
import com.studies.rrbmustudies.ui.components.AdminFab
import com.studies.rrbmustudies.ui.components.EmptyState
import com.studies.rrbmustudies.ui.components.ErrorState
import com.studies.rrbmustudies.ui.components.LoadingShimmer
import com.studies.rrbmustudies.ui.state.UiState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCoursesScreen(
    onBack: () -> Unit,
    viewModel: ManageCoursesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.actionMessage.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<Course?>(null) }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Courses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        editingCourse = Course(
                            id = "",
                            name = "",
                            shortName = "",
                            order = 0,
                            level = CourseLevel.UG,
                            isActive = true,
                        )
                        showDialog = true
                    }) { Text("Add") }
                },
            )
        },
        floatingActionButton = {
            AdminFab(
                onClick = {
                    editingCourse = Course(
                        id = "",
                        name = "",
                        shortName = "",
                        order = 0,
                        level = CourseLevel.UG,
                        isActive = true,
                    )
                    showDialog = true
                },
                contentDescription = "Add course",
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
                if (uiState.data.isEmpty()) {
                    EmptyState(
                        title = "No courses yet",
                        message = "Add a course to publish it for students.",
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(padding)) {
                        message?.let {
                            item {
                                Text(
                                    it,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        items(uiState.data) { course ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clickable {
                                        editingCourse = course
                                        showDialog = true
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(course.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${course.shortName} • ${course.level.name} • Order ${course.order}",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                        Text(
                                            if (course.isActive) "Active" else "Inactive",
                                            color = if (course.isActive) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.error
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && editingCourse != null) {
        CourseEditDialog(
            course = editingCourse!!,
            isNew = editingCourse!!.id.isBlank(),
            isUploadingBackground = viewModel.isUploadingBackground.collectAsStateWithLifecycle().value,
            uploadedBackgroundUrl = viewModel.uploadedBackgroundUrl.collectAsStateWithLifecycle().value,
            onDismiss = {
                showDialog = false
                viewModel.clearUploadedBackground()
            },
            onPickBackground = { file ->
                val id = editingCourse!!.id.ifBlank { "new" }
                viewModel.uploadBackground(file.bytes, file.fileName, id)
            },
            onSave = { course ->
                viewModel.saveCourse(course, isNew = editingCourse!!.id.isBlank())
                showDialog = false
            },
        )
    }
}

@Composable
private fun CourseEditDialog(
    course: Course,
    isNew: Boolean,
    isUploadingBackground: Boolean,
    uploadedBackgroundUrl: String?,
    onDismiss: () -> Unit,
    onPickBackground: (PickedFile) -> Unit,
    onSave: (Course) -> Unit,
) {
    var courseId by remember(course.id) { mutableStateOf(course.id) }
    var name by remember(course.id) { mutableStateOf(course.name) }
    var shortName by remember(course.id) { mutableStateOf(course.shortName) }
    var level by remember(course.id) { mutableStateOf(course.level) }
    var order by remember(course.id) { mutableStateOf(course.order.toString()) }
    var durationYears by remember(course.id) { mutableStateOf(course.durationYears.toString()) }
    var iconUrl by remember(course.id) { mutableStateOf(course.iconUrl.orEmpty()) }
    var backgroundImageUrl by remember(course.id, uploadedBackgroundUrl) {
        mutableStateOf(uploadedBackgroundUrl ?: course.backgroundImageUrl.orEmpty())
    }
    LaunchedEffect(uploadedBackgroundUrl) {
        if (!uploadedBackgroundUrl.isNullOrBlank()) backgroundImageUrl = uploadedBackgroundUrl
    }
    var isActive by remember(course.id) { mutableStateOf(course.isActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "New Course" else "Edit Course") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isNew) {
                    OutlinedTextField(
                        value = courseId,
                        onValueChange = { courseId = it },
                        label = { Text("Course ID (e.g. bsc)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                OutlinedTextField(name, { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(shortName, { shortName = it }, label = { Text("Short name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(order, { order = it }, label = { Text("Display order") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    durationYears,
                    { durationYears = it.filter { ch -> ch.isDigit() }.take(1) },
                    label = { Text("Duration (years)") },
                    supportingText = { Text("2 → Sem 1–4, 3 → Sem 1–6") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(iconUrl, { iconUrl = it }, label = { Text("Icon URL (optional)") }, modifier = Modifier.fillMaxWidth())
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isUploadingBackground -> Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                            Text("Uploading…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        backgroundImageUrl.isNotBlank() -> coil3.compose.AsyncImage(
                            model = backgroundImageUrl,
                            contentDescription = "Course background preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        else -> Text(
                            "No background image",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                ImagePickerButton(
                    label = if (isUploadingBackground) "Uploading background…" else "Pick background image (optional)",
                    onImagePicked = onPickBackground,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploadingBackground,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CourseLevel.entries.forEach { option ->
                        TextButton(onClick = { level = option }) {
                            Text(
                                option.name,
                                color = if (level == option) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Active")
                    Switch(isActive, { isActive = it }, modifier = Modifier.padding(start = 8.dp))
                }
                if (isNew) {
                    Text(
                        "Creating a course seeds Yearly (Part 1–3), Semester (based on duration years), and Entrance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    course.copy(
                        id = if (isNew) courseId else course.id,
                        name = name.trim(),
                        shortName = shortName.trim(),
                        level = level,
                        order = order.toIntOrNull() ?: 0,
                        durationYears = (durationYears.toIntOrNull() ?: 3).coerceIn(1, 5),
                        iconUrl = iconUrl.trim().ifBlank { null },
                        backgroundImageUrl = backgroundImageUrl.trim().ifBlank { null },
                        isActive = isActive,
                    ),
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
