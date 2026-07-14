package com.studies.rrbmustudies.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.domain.model.AppNotification
import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.ui.components.RrbmuTopBar
import com.studies.rrbmustudies.ui.components.SaffronButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.studies.rrbmustudies.ui.components.StitchBreadcrumb
import com.studies.rrbmustudies.ui.state.UiState
import com.studies.rrbmustudies.ui.theme.RrbmuDimens
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AdminLoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Login") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Sign in with your administrator account. Accounts are created manually in Firebase Console.",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            SaffronButton(
                text = if (state.isLoading) "Signing in…" else "Sign In",
                onClick = viewModel::signIn,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit,
    onManageCourses: () -> Unit,
    onManageAds: () -> Unit,
    onManageNotifications: () -> Unit,
    onUploadPaper: () -> Unit,
    onSignOut: () -> Unit,
    adminEmail: String?,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            adminEmail?.let {
                Text(
                    "Signed in as $it",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            DashboardItem("Manage Courses", Icons.Default.MenuBook, onManageCourses)
            HorizontalDivider()
            DashboardItem("Manage Home Ads", Icons.Default.Campaign, onManageAds)
            HorizontalDivider()
            DashboardItem("Manage Notifications", Icons.Default.Notifications, onManageNotifications)
            HorizontalDivider()
            DashboardItem("Upload Paper", Icons.Default.UploadFile, onUploadPaper)
            HorizontalDivider()
            DashboardItem("Sign Out", Icons.Default.Logout, onSignOut)
        }
    }
}

@Composable
private fun DashboardItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, null) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHomeAdsScreen(
    onBack: () -> Unit,
    viewModel: ManageHomeAdsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.actionMessage.collectAsStateWithLifecycle()
    val isUploadingImage by viewModel.isUploadingImage.collectAsStateWithLifecycle()
    val uploadedImageUrl by viewModel.uploadedImageUrl.collectAsStateWithLifecycle()
    val carouselSettings by viewModel.carouselSettings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }
    var editingAd by remember { mutableStateOf<HomeAd?>(null) }

    LaunchedEffect(message) {
        val msg = message ?: return@LaunchedEffect
        if (msg == "Ad saved" || msg == "Ad deleted") {
            showDialog = false
            viewModel.clearUploadedImageUrl()
        }
        snackbarHostState.showSnackbar(msg)
        viewModel.clearMessage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Banners") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingAd = HomeAd(id = "", imageUrl = "", title = "", linkUrl = "", order = 0)
                    showDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New banner") },
            )
        },
    ) { padding ->
        when (val uiState = state) {
            is UiState.Loading -> com.studies.rrbmustudies.ui.components.LoadingShimmer(modifier = Modifier.padding(padding))
            is UiState.Error -> com.studies.rrbmustudies.ui.components.ErrorState(
                uiState.message, uiState.retry ?: viewModel::refresh, Modifier.padding(padding),
            )
            is UiState.Success -> {
                val ads = uiState.data
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = RrbmuDimens.screenHorizontal,
                        end = RrbmuDimens.screenHorizontal,
                        top = 12.dp,
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        CarouselTimingCard(
                            seconds = carouselSettings.slideIntervalSeconds,
                            slideCount = ads.size,
                            onSave = viewModel::saveCarouselInterval,
                        )
                    }
                    item {
                        Text(
                            text = "Banners  •  ${ads.size}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (ads.isEmpty()) {
                        item {
                            HomeAdsEmptyState()
                        }
                    } else {
                        itemsIndexed(ads, key = { _, ad -> ad.id }) { index, ad ->
                            HomeAdManageCard(
                                ad = ad,
                                position = index + 1,
                                total = ads.size,
                                onEdit = { editingAd = ad; showDialog = true },
                                onDelete = { viewModel.deleteAd(ad.id) },
                                onMoveTop = { viewModel.moveAdToTop(ad.id) },
                                onMoveUp = { viewModel.moveAdUp(ad.id) },
                                onMoveDown = { viewModel.moveAdDown(ad.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog && editingAd != null) {
        HomeAdEditDialog(
            ad = editingAd!!,
            isUploadingImage = isUploadingImage,
            freshlyUploadedUrl = uploadedImageUrl,
            onDismiss = {
                showDialog = false
                viewModel.clearUploadedImageUrl()
            },
            onPickImage = { file -> viewModel.uploadImage(file.bytes, file.fileName) },
            onSave = { viewModel.saveAd(it) },
            onDelete = editingAd!!.id.takeIf { it.isNotBlank() }?.let { id ->
                { viewModel.deleteAd(id) }
            },
        )
    }
}

@Composable
private fun CarouselTimingCard(
    seconds: Int,
    slideCount: Int,
    onSave: (Int) -> Unit,
) {
    var value by remember(seconds) { mutableStateOf(seconds.coerceIn(2, 60)) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RrbmuDimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(RrbmuDimens.spacingMd), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text("Slide duration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                if (slideCount <= 1) {
                    "Applies to all banners. Auto-scroll is off while there is only one banner."
                } else {
                    "Each banner is shown for this many seconds before sliding."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalIconButton(
                    onClick = { if (value > 2) value -= 1 },
                    enabled = value > 2,
                ) { Icon(Icons.Default.Remove, contentDescription = "Decrease") }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "$value s",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                FilledTonalIconButton(
                    onClick = { if (value < 60) value += 1 },
                    enabled = value < 60,
                ) { Icon(Icons.Default.Add, contentDescription = "Increase") }
            }
            SaffronButton(
                text = if (value == seconds) "Saved" else "Save duration",
                onClick = { onSave(value) },
                enabled = value != seconds,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HomeAdManageCard(
    ad: HomeAd,
    position: Int,
    total: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveTop: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RrbmuDimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 7f)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center,
            ) {
                if (ad.imageUrl.isNotBlank()) {
                    coil3.compose.AsyncImage(
                        model = ad.imageUrl,
                        contentDescription = ad.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp),
                    )
                }
                StatusChip(
                    active = ad.isActive,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RrbmuDimens.spacingMd, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$position",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    ad.title.ifBlank { "Untitled banner" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onMoveTop, enabled = position > 1) {
                    Icon(Icons.Default.KeyboardDoubleArrowUp, contentDescription = "Move to top")
                }
                IconButton(onClick = onMoveUp, enabled = position > 1) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(onClick = onMoveDown, enabled = position < total) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(active: Boolean, modifier: Modifier = Modifier) {
    val bg = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg.copy(alpha = 0.92f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (active) "Active" else "Hidden",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@Composable
private fun HomeAdsEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Campaign,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            )
        }
        Text("No banners yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Tap “New banner” to add a promotional slide to the Home carousel.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun HomeAdEditDialog(
    ad: HomeAd,
    isUploadingImage: Boolean,
    freshlyUploadedUrl: String?,
    onDismiss: () -> Unit,
    onPickImage: (PickedFile) -> Unit,
    onSave: (HomeAd) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val resolvedImage = listOfNotNull(
        freshlyUploadedUrl?.takeIf { it.isNotBlank() },
        ad.imageUrl.takeIf { it.isNotBlank() },
    ).firstOrNull().orEmpty()
    var imageUrl by remember(ad.id, freshlyUploadedUrl) { mutableStateOf(resolvedImage) }
    LaunchedEffect(freshlyUploadedUrl, ad.imageUrl) {
        val latest = freshlyUploadedUrl?.takeIf { it.isNotBlank() } ?: ad.imageUrl
        if (latest.isNotBlank()) imageUrl = latest
    }
    var title by remember(ad.id) { mutableStateOf(ad.title) }
    var description by remember(ad.id) { mutableStateOf(ad.description.orEmpty()) }
    var linkUrl by remember(ad.id) { mutableStateOf(ad.linkUrl) }
    var isActive by remember(ad.id) { mutableStateOf(ad.isActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ad.id.isBlank()) "New Ad" else "Edit Ad") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isUploadingImage -> Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                            )
                            Text("Uploading image…", color = MaterialTheme.colorScheme.primary)
                        }
                        imageUrl.isNotBlank() -> coil3.compose.AsyncImage(
                            model = imageUrl,
                            contentDescription = "Ad banner preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        )
                        else -> Text("No image selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                ImagePickerButton(
                    label = if (isUploadingImage) "Uploading…" else "Pick banner from gallery",
                    onImagePicked = onPickImage,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploadingImage,
                )
                OutlinedTextField(
                    title,
                    { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    description,
                    { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    linkUrl,
                    { linkUrl = it },
                    label = { Text("Link URL (https://…)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text("Opens when students tap the ad on Home")
                    },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Active")
                    Switch(isActive, { isActive = it }, modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        ad.copy(
                            imageUrl = imageUrl.ifBlank { freshlyUploadedUrl.orEmpty() },
                            title = title,
                            description = description.ifBlank { null },
                            linkUrl = linkUrl.trim(),
                            isActive = isActive,
                        ),
                    )
                },
                enabled = !isUploadingImage,
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                onDelete?.let {
                    TextButton(onClick = it) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageNotificationsAdminScreen(
    onBack: () -> Unit,
    viewModel: ManageNotificationsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.actionMessage.collectAsStateWithLifecycle()
    val isUploadingAttachment by viewModel.isUploadingAttachment.collectAsStateWithLifecycle()
    val uploadedAttachmentUrl by viewModel.uploadedAttachmentUrl.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AppNotification?>(null) }

    LaunchedEffect(message) {
        val msg = message ?: return@LaunchedEffect
        if (msg == "Notification saved" || msg == "Deleted") {
            showDialog = false
            viewModel.clearUploadedAttachment()
        }
        kotlinx.coroutines.delay(2000)
        viewModel.clearMessage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        editing = AppNotification(id = "", title = "", body = "")
                        showDialog = true
                    }) {
                        Text("Add")
                    }
                },
            )
        },
    ) { padding ->
        when (val uiState = state) {
            is UiState.Loading -> com.studies.rrbmustudies.ui.components.LoadingShimmer(modifier = Modifier.padding(padding))
            is UiState.Error -> com.studies.rrbmustudies.ui.components.ErrorState(
                uiState.message, uiState.retry ?: viewModel::refresh, Modifier.padding(padding),
            )
            is UiState.Success -> LazyColumn(modifier = Modifier.padding(padding)) {
                message?.let {
                    item {
                        Text(it, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                    }
                }
                items(uiState.data) { n ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { editing = n; showDialog = true },
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(n.title, style = MaterialTheme.typography.titleMedium)
                            Text(n.body, maxLines = 2)
                            Text(
                                buildString {
                                    append(if (n.isActive) "Active" else "Draft")
                                    if (n.hasAttachment) append(" · Has file")
                                },
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog && editing != null) {
        NotificationEditDialog(
            notification = editing!!,
            isUploadingAttachment = isUploadingAttachment,
            freshlyUploadedUrl = uploadedAttachmentUrl,
            onDismiss = {
                showDialog = false
                viewModel.clearUploadedAttachment()
            },
            onPickAttachment = { file -> viewModel.uploadAttachment(file.bytes, file.fileName) },
            onSave = { viewModel.save(it) },
            onDelete = editing!!.id.takeIf { it.isNotBlank() }?.let { id ->
                { viewModel.delete(id) }
            },
        )
    }
}

@Composable
private fun NotificationEditDialog(
    notification: AppNotification,
    isUploadingAttachment: Boolean,
    freshlyUploadedUrl: String?,
    onDismiss: () -> Unit,
    onPickAttachment: (PickedFile) -> Unit,
    onSave: (AppNotification) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var title by remember(notification.id) { mutableStateOf(notification.title) }
    var body by remember(notification.id) { mutableStateOf(notification.body) }
    var attachmentUrl by remember(notification.id) {
        mutableStateOf(notification.resolvedAttachmentUrl.orEmpty())
    }
    var isActive by remember(notification.id) { mutableStateOf(notification.isActive) }

    LaunchedEffect(freshlyUploadedUrl) {
        if (!freshlyUploadedUrl.isNullOrBlank()) {
            attachmentUrl = freshlyUploadedUrl
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (notification.id.isBlank()) "New Notification" else "Edit Notification") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    body,
                    { body = it },
                    label = { Text("Body") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
                Text(
                    "Attachment (optional PDF / image)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                NotificationAttachmentPickerButton(
                    label = if (isUploadingAttachment) {
                        "Uploading…"
                    } else if (attachmentUrl.isNotBlank()) {
                        "Change attached file"
                    } else {
                        "Attach PDF or image"
                    },
                    onFilePicked = onPickAttachment,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploadingAttachment,
                )
                if (attachmentUrl.isNotBlank()) {
                    Text(
                        "File ready — students can open it from notification details.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Publish / Active")
                    Switch(isActive, { isActive = it }, modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isUploadingAttachment,
                onClick = {
                    onSave(
                        notification.copy(
                            title = title,
                            body = body,
                            attachmentUrl = attachmentUrl.ifBlank { null },
                            linkUrl = attachmentUrl.ifBlank { null },
                            isActive = isActive,
                        ),
                    )
                },
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                onDelete?.let {
                    TextButton(onClick = it) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPaperScreen(
    onBack: () -> Unit,
    onUploadSuccess: () -> Unit,
    viewModel: UploadPaperViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    )
    val fieldShape = RoundedCornerShape(RrbmuDimens.buttonRadius)

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onUploadSuccess()
    }

    Scaffold(
        topBar = { RrbmuTopBar(showBack = true, onBack = onBack) },
        bottomBar = {
            UploadPaperBottomBar(
                isUploading = state.isUploading,
                onSaveDraft = viewModel::saveDraft,
                onSaveAndPublish = viewModel::saveAndPublish,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = RrbmuDimens.screenHorizontal)
                .padding(top = RrbmuDimens.spacingMd)
                .padding(bottom = RrbmuDimens.spacingLg),
            verticalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingMd),
        ) {
            StitchBreadcrumb(segments = listOf("Dashboard", "Upload New Paper"))
            Text(
                "Upload New Paper",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Add a new academic resource. Fill location, identity, and attach a PDF — then save as draft or publish.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AdminFormCard(title = "Paper Location", icon = Icons.Default.LocationOn) {
                UploadLocationDropdown(
                    label = "Course",
                    value = state.selectedCourse?.shortName ?: "Select course",
                    enabled = state.courses.isNotEmpty(),
                    options = state.courses.map { it.shortName to it.id },
                    onSelected = viewModel::onCourseSelected,
                    fieldColors = fieldColors,
                    fieldShape = fieldShape,
                )
                UploadLocationDropdown(
                    label = "System",
                    value = state.selectedSystem?.name ?: "Select system",
                    enabled = state.courseId.isNotBlank() && state.systems.isNotEmpty(),
                    options = state.systems.map { it.name to it.id },
                    onSelected = viewModel::onSystemSelected,
                    fieldColors = fieldColors,
                    fieldShape = fieldShape,
                )
                UploadLocationDropdown(
                    label = "Part / Semester",
                    value = state.selectedPart?.name ?: "Select part",
                    enabled = state.systemId.isNotBlank() && state.parts.isNotEmpty(),
                    options = state.parts.map { it.name to it.id },
                    onSelected = viewModel::onPartSelected,
                    fieldColors = fieldColors,
                    fieldShape = fieldShape,
                )
            }
            AdminFormCard(title = "Paper Identity", icon = Icons.Default.Description) {
                StitchField("Paper Title", state.title, viewModel::onTitleChange, fieldColors, fieldShape)
                StitchField("Subject", state.subject, viewModel::onSubjectChange, fieldColors, fieldShape)
                StitchField("Paper Code", state.paperCode, viewModel::onPaperCodeChange, fieldColors, fieldShape, "CODE-101")
                StitchField("Exam Year", state.year, viewModel::onYearChange, fieldColors, fieldShape, "2024")
                StitchField(
                    "Description (optional)",
                    state.description,
                    viewModel::onDescriptionChange,
                    fieldColors,
                    fieldShape,
                    minLines = 2,
                )
            }
            AdminFormCard(title = "Paper Document", icon = Icons.Default.UploadFile) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(RrbmuDimens.cardRadius))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(RrbmuDimens.cardRadius),
                        )
                        .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f))
                        .padding(RrbmuDimens.spacingLg),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                .padding(16.dp),
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (state.pickedFile != null) "PDF selected" else "Tap to select PDF",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            state.pickedFile?.fileName
                                ?: "Maximum file size: 25MB. PDF format only.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        PdfPickerButton(
                            label = if (state.pickedFile != null) "Change PDF" else "Select PDF",
                            onFilePicked = viewModel::onFilePicked,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun UploadPaperBottomBar(
    isUploading: Boolean,
    onSaveDraft: () -> Unit,
    onSaveAndPublish: () -> Unit,
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = RrbmuDimens.screenHorizontal,
                    vertical = RrbmuDimens.spacingMd,
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onSaveDraft,
                enabled = !isUploading,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
            ) {
                Text(if (isUploading) "Saving…" else "Save Draft")
            }
            SaffronButton(
                text = if (isUploading) "Publishing…" else "Save & Publish",
                onClick = onSaveAndPublish,
                enabled = !isUploading,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                leading = if (isUploading) null else {
                    { Icon(Icons.Default.Publish, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White) }
                },
            )
        }
    }
}

@Composable
private fun UploadLocationDropdown(
    label: String,
    value: String,
    enabled: Boolean,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors,
    fieldShape: RoundedCornerShape,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(fieldShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    },
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = "Open $label menu",
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    },
                )
            }
            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { (name, id) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            expanded = false
                            onSelected(id)
                        },
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun AdminFormCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RrbmuDimens.cardRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(RrbmuDimens.cardRadius),
            )
            .padding(RrbmuDimens.spacingLg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun StitchField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    colors: androidx.compose.material3.TextFieldColors,
    shape: RoundedCornerShape,
    placeholder: String = "",
    minLines: Int = 1,
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = if (placeholder.isNotBlank()) ({ Text(placeholder) }) else null,
            minLines = minLines,
            shape = shape,
            colors = colors,
        )
    }
}
