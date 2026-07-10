package com.studies.rrbmustudies.presentation.notifications

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.studies.rrbmustudies.navigation.AppDeepLinks
import com.studies.rrbmustudies.platform.openUrl
import com.studies.rrbmustudies.platform.shareText
import com.studies.rrbmustudies.ui.components.AdBannerSlot
import com.studies.rrbmustudies.ui.components.NotificationTypeBadge
import com.studies.rrbmustudies.ui.components.RrbmuTopBar
import com.studies.rrbmustudies.ui.theme.RrbmuDimens
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NotificationDetailScreen(
    notificationId: String,
    onBack: () -> Unit,
    openAttachment: Boolean = false,
    onOpenPdf: (url: String, title: String) -> Unit = { _, _ -> },
    onViewPaperList: () -> Unit = {},
    viewModel: NotificationDetailViewModel = koinViewModel { parametersOf(notificationId) },
) {
    val notification by viewModel.notification.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            RrbmuTopBar(
                showBack = true,
                onBack = onBack,
            )
        },
    ) { padding ->
        val item = notification
        if (item == null) {
            BoxLoading(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        val attachmentUrl = item.resolvedAttachmentUrl
        val hasAttachment = !attachmentUrl.isNullOrBlank()
        val isImage = attachmentUrl?.substringBefore('?')?.lowercase()?.let { path ->
            path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") ||
                path.endsWith(".webp") || path.endsWith(".gif")
        } == true
        val isPdf = attachmentUrl?.substringBefore('?')?.lowercase()?.let { path ->
            path.endsWith(".pdf") || path.contains("/o/notification_attachments")
        } == true && !isImage

        fun openAttachmentFile() {
            val url = attachmentUrl ?: return
            when {
                isImage -> openUrl(url)
                else -> onOpenPdf(url, item.title)
            }
        }

        // From a tray tap with a file: open PDF in-app viewer once loaded.
        LaunchedEffect(item.id, openAttachment, attachmentUrl, isImage) {
            val url = attachmentUrl
            if (openAttachment && !url.isNullOrBlank() && !isImage) {
                onOpenPdf(url, item.title)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = RrbmuDimens.screenHorizontal, vertical = RrbmuDimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingLg),
        ) {
            NotificationTypeBadge(category = item.category)

            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatNotificationTime(item.createdAt),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = item.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isImage && !attachmentUrl.isNullOrBlank()) {
                AsyncImage(
                    model = attachmentUrl,
                    contentDescription = "Attached image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(RrbmuDimens.cardRadius)),
                )
            }

            Button(
                onClick = { openAttachmentFile() },
                enabled = hasAttachment,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
            ) {
                Icon(
                    if (isImage) Icons.Default.Image else Icons.Default.PictureAsPdf,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    when {
                        !hasAttachment -> "See file (none attached)"
                        isImage -> "See image"
                        isPdf -> "See PDF"
                        else -> "See file / PDF"
                    },
                )
            }

            if (!hasAttachment) {
                Text(
                    text = "No file was attached to this alert.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedButton(
                onClick = {
                    shareText(
                        AppDeepLinks.shareNotification(
                            title = item.title,
                            body = item.body,
                            notificationId = item.id,
                        ),
                        item.title,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Share")
            }

            Button(
                onClick = onViewPaperList,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("View Paper List")
            }

            AdBannerSlot()
        }
    }
}

@Composable
private fun BoxLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
