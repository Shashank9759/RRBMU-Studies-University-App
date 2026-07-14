package com.studies.rrbmustudies.presentation.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.studies.rrbmustudies.platform.openUrl
import com.studies.rrbmustudies.platform.shareText
import com.studies.rrbmustudies.ui.components.AdBannerSlot
import com.studies.rrbmustudies.ui.components.GuestUserCard
import com.studies.rrbmustudies.ui.components.MoreBrandedHeader
import com.studies.rrbmustudies.ui.components.MoreMenuRow
import com.studies.rrbmustudies.ui.components.MoreSectionHeader
import com.studies.rrbmustudies.ui.theme.RrbmuDimens
import com.studies.rrbmustudies.ui.theme.UsefulLinks

private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.studies.rrbmustudies"

@Composable
fun MoreScreen(
    isAdmin: Boolean,
    onNavigateToFeedback: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenWebView: (url: String, title: String) -> Unit,
    onAdminDashboard: () -> Unit,
    onLoginClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = RrbmuDimens.screenHorizontal,
            vertical = RrbmuDimens.spacingMd,
        ),
        verticalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingLg),
    ) {
        item { MoreBrandedHeader() }

        if (isAdmin) {
            item {
                GuestUserCard(
                    isAdmin = true,
                    onLoginClick = onLoginClick,
                    onManageContent = onAdminDashboard,
                )
            }
        }

        item {
            MoreSectionHeader("University Services")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(RrbmuDimens.cardRadius))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            ) {
                UsefulLinks.forEachIndexed { index, link ->
                    val icon = when (index) {
                        0 -> Icons.Default.Badge
                        1 -> Icons.Default.School
                        else -> Icons.Default.Link
                    }
                    MoreMenuRow(
                        title = link.title,
                        subtitle = when (index) {
                            0 -> "Official exam updates"
                            1 -> "Access student portal"
                            else -> "Resources & notifications"
                        },
                        icon = icon,
                        onClick = { onOpenWebView(link.url, link.title) },
                    )
                    if (index < UsefulLinks.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        )
                    }
                }
            }
        }

        item {
            MoreSectionHeader("App")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(RrbmuDimens.cardRadius))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            ) {
                MoreMenuRow(
                    title = "Feedback",
                    icon = Icons.Default.Star,
                    onClick = onNavigateToFeedback,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                MoreMenuRow(
                    title = "About Team",
                    icon = Icons.Default.Groups,
                    onClick = onNavigateToAbout,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                MoreMenuRow(
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    onClick = onNavigateToSettings,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                MoreMenuRow(
                    title = "Share App",
                    subtitle = "Tell your classmates",
                    icon = Icons.Default.Share,
                    onClick = {
                        shareText(
                            text = "Get RRBMU previous-year question papers on RRBMU Studies: $PLAY_STORE_URL",
                            title = "Share RRBMU Studies",
                        )
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                MoreMenuRow(
                    title = "Rate Us",
                    subtitle = "Rate us on the Play Store",
                    icon = Icons.Default.StarRate,
                    onClick = { openUrl(PLAY_STORE_URL) },
                )
            }
        }

        item { AdBannerSlot() }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = RrbmuDimens.spacingLg),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "RRBMU Studies",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
