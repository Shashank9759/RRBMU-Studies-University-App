package com.studies.rrbmustudies.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.ui.components.AdminBadge
import com.studies.rrbmustudies.ui.components.AdminLoginBottomSheet
import com.studies.rrbmustudies.ui.components.RrbmuDialog
import com.studies.rrbmustudies.ui.components.RrbmuTopBar
import com.studies.rrbmustudies.ui.components.SettingsCard
import com.studies.rrbmustudies.ui.components.SettingsRow
import com.studies.rrbmustudies.ui.components.SettingsSectionHeader
import com.studies.rrbmustudies.ui.components.SettingsToggle
import com.studies.rrbmustudies.ui.components.ThemeSegmentedControl
import com.studies.rrbmustudies.ui.theme.AboutDefaults
import com.studies.rrbmustudies.ui.theme.RrbmuDimens
import com.studies.rrbmustudies.ui.theme.StitchError
import org.koin.compose.viewmodel.koinViewModel
import com.studies.rrbmustudies.presentation.admin.AdminLoginViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onManageAds: () -> Unit,
    onManageNotifications: () -> Unit,
    onDownloadedPapers: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
    adminLoginViewModel: AdminLoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adminLoginState by adminLoginViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(adminLoginState.isSuccess) {
        if (adminLoginState.isSuccess) {
            viewModel.onAdminLoginSuccess()
            adminLoginViewModel.reset()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Snackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            RrbmuTopBar(showBack = true, title = "Settings", onBack = onBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (!uiState.isAdminResolved) {
            // Wait for admin state before first paint so the layout doesn't jump when the
            // admin Account section appears at the top.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = RrbmuDimens.screenHorizontal, vertical = RrbmuDimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(RrbmuDimens.sectionGap),
        ) {
            if (uiState.isAdmin) {
                SettingsSectionHeader("Account")
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(RrbmuDimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingMd),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = uiState.adminUser?.email?.take(2)?.uppercase() ?: "AD",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "Administrator",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                AdminBadge()
                            }
                            Text(
                                text = uiState.adminUser?.email ?: "admin@rrbmu.com",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                SettingsCard {
                    SettingsRow(
                        title = "Manage Home Ads",
                        icon = Icons.Default.AdsClick,
                        onClick = onManageAds,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    SettingsRow(
                        title = "Manage Notifications",
                        icon = Icons.Default.Campaign,
                        onClick = onManageNotifications,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    SettingsRow(
                        title = "Logout",
                        icon = Icons.Default.Logout,
                        onClick = viewModel::showLogoutDialog,
                        titleColor = StitchError,
                    )
                }
            }

            SettingsSectionHeader("App Preferences")
            SettingsCard {
                Column(modifier = Modifier.padding(RrbmuDimens.spacingMd)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = when (uiState.themeMode) {
                            com.studies.rrbmustudies.domain.model.ThemeMode.LIGHT -> "Light Mode"
                            com.studies.rrbmustudies.domain.model.ThemeMode.DARK -> "Dark Mode"
                            com.studies.rrbmustudies.domain.model.ThemeMode.SYSTEM -> "System"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ThemeSegmentedControl(
                        selected = uiState.themeMode,
                        onSelected = viewModel::setThemeMode,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                SettingsRow(
                    title = "Push Notifications",
                    subtitle = "Get alerts for new courses",
                    icon = Icons.Default.NotificationsActive,
                    trailing = {
                        SettingsToggle(
                            checked = uiState.pushNotificationsEnabled,
                            onCheckedChange = viewModel::setPushNotifications,
                        )
                    },
                )
            }

            SettingsSectionHeader("Storage")
            SettingsCard {
                SettingsRow(
                    title = "Downloaded Papers",
                    subtitle = "${uiState.downloadedPapersCount} files • ${uiState.downloadedPapersSizeMb}",
                    icon = Icons.Default.Description,
                    onClick = onDownloadedPapers,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                SettingsRow(
                    title = "Clear Downloads",
                    subtitle = "Delete offline papers from app vault",
                    icon = Icons.Default.DeleteSweep,
                    onClick = viewModel::showClearDownloadsDialog,
                    trailing = {
                        Text(
                            text = uiState.downloadedPapersSizeMb,
                            style = MaterialTheme.typography.labelMedium,
                            color = StitchError,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                )
            }

            if (!uiState.isAdmin) {
                SettingsSectionHeader("Account")
                SettingsCard {
                    SettingsRow(
                        title = "Login as Administrator",
                        subtitle = "Upload and manage content",
                        icon = Icons.Default.Lock,
                        onClick = viewModel::showAdminLogin,
                    )
                }
            }

            Text(
                text = "RRBMU Studies v${AboutDefaults.APP_VERSION} (Stable)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = RrbmuDimens.spacingMd),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }

    AdminLoginBottomSheet(
        visible = uiState.showAdminLoginSheet,
        uiState = adminLoginState,
        onDismiss = viewModel::dismissAdminLogin,
        onEmailChange = adminLoginViewModel::onEmailChange,
        onPasswordChange = adminLoginViewModel::onPasswordChange,
        onTogglePasswordVisibility = adminLoginViewModel::togglePasswordVisibility,
        onLogin = adminLoginViewModel::signIn,
    )

    if (uiState.showLogoutDialog) {
        RrbmuDialog(
            title = "Sign Out?",
            message = "You will lose administrator access until you sign in again.",
            confirmLabel = "Sign Out",
            onConfirm = viewModel::confirmLogout,
            onDismiss = viewModel::dismissLogoutDialog,
            destructive = true,
        )
    }

    if (uiState.showClearDownloadsDialog) {
        RrbmuDialog(
            title = "Clear downloaded papers?",
            message = "Downloaded papers will be deleted. You will need to download them again to view offline.",
            confirmLabel = "Delete",
            onConfirm = viewModel::clearDownloadedPapers,
            onDismiss = viewModel::dismissClearDownloadsDialog,
            destructive = true,
        )
    }
}
