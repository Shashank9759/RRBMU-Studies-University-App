package com.studies.rrbmustudies.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.studies.rrbmustudies.domain.model.AppNotification
import com.studies.rrbmustudies.domain.model.NotificationCategory
import com.studies.rrbmustudies.ui.theme.RrbmuDimens
import com.studies.rrbmustudies.ui.theme.StitchError
import com.studies.rrbmustudies.ui.theme.StitchSecondary
import com.studies.rrbmustudies.ui.theme.StitchSecondaryContainer

@Composable
fun NotificationTypeBadge(
    category: NotificationCategory,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = when (category) {
        NotificationCategory.ANNOUNCEMENT -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) to
            MaterialTheme.colorScheme.primary
        NotificationCategory.TIME_TABLE -> StitchSecondaryContainer.copy(alpha = 0.2f) to StitchSecondary
        NotificationCategory.IMPORTANT -> MaterialTheme.colorScheme.errorContainer to StitchError
    }
    Text(
        text = category.label.uppercase(),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = fg,
    )
}

@Composable
fun NotificationItem(
    notification: AppNotification,
    isUnread: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    timestamp: String,
) {
    val icon = categoryIcon(notification.category)
    val iconTint = categoryTint(notification.category)
    val iconBg = categoryIconBackground(notification.category)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RrbmuDimens.cardRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(RrbmuDimens.cardRadius),
            )
            .clickable(onClick = onClick)
            .padding(RrbmuDimens.spacingMd),
        horizontalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingMd),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingSm),
        ) {
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
fun EmptyNotificationState(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(RrbmuDimens.spacingXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingMd),
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(80.dp),
            )
        }
        Text(
            text = "No notifications yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "We'll notify you when something new arrives",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun categoryIcon(category: NotificationCategory): ImageVector = when (category) {
    NotificationCategory.ANNOUNCEMENT -> Icons.Default.Campaign
    NotificationCategory.TIME_TABLE -> Icons.Default.CalendarMonth
    NotificationCategory.IMPORTANT -> Icons.Default.PriorityHigh
}

@Composable
private fun categoryTint(category: NotificationCategory): Color = when (category) {
    NotificationCategory.ANNOUNCEMENT -> MaterialTheme.colorScheme.primary
    NotificationCategory.TIME_TABLE -> StitchSecondary
    NotificationCategory.IMPORTANT -> StitchError
}

@Composable
private fun categoryIconBackground(category: NotificationCategory): Color = when (category) {
    NotificationCategory.ANNOUNCEMENT -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    NotificationCategory.TIME_TABLE -> StitchSecondaryContainer.copy(alpha = 0.2f)
    NotificationCategory.IMPORTANT -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
}
