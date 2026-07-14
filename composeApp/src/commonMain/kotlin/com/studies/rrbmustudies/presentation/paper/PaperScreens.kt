package com.studies.rrbmustudies.presentation.paper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.navigation.AppDeepLinks
import com.studies.rrbmustudies.platform.shareText
import com.studies.rrbmustudies.ui.components.AdBannerSlot
import com.studies.rrbmustudies.ui.components.CrestWatermark
import com.studies.rrbmustudies.ui.components.ErrorState
import com.studies.rrbmustudies.ui.components.LoadingShimmer
import com.studies.rrbmustudies.ui.components.RrbmuTopBar
import com.studies.rrbmustudies.ui.components.SaffronButton
import com.studies.rrbmustudies.ui.state.UiState
import com.studies.rrbmustudies.ui.theme.RrbmuDimens
import com.studies.rrbmustudies.ui.theme.courseHeroBrush
import com.studies.rrbmustudies.ui.theme.courseJewel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaperDetailScreen(
    onBack: () -> Unit,
    onOpenPdf: (String, String) -> Unit,
    viewModel: PaperDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PaperDetailEvent.OpenPdf -> onOpenPdf(event.pathOrUrl, event.title)
                is PaperDetailEvent.Snackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { RrbmuTopBar(showBack = true, onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (val uiState = state) {
            is UiState.Loading -> LoadingShimmer(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(
                message = uiState.message,
                onRetry = uiState.retry ?: viewModel::refresh,
                modifier = Modifier.padding(padding),
            )
            is UiState.Success -> {
                val detail = uiState.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    PaperHeroBanner(paper = detail.paper)
                    Column(
                        modifier = Modifier
                            .padding(horizontal = RrbmuDimens.screenHorizontal)
                            .offsetCard(),
                    ) {
                        DetailCard(
                            paper = detail.paper,
                            isDownloaded = detail.isDownloaded,
                            isDownloading = detail.isDownloading,
                            downloadError = detail.downloadError,
                            onViewPdf = viewModel::openPdf,
                            onDownloadOffline = viewModel::downloadToVault,
                            onShare = {
                                val paper = detail.paper
                                shareText(
                                    AppDeepLinks.sharePaper(
                                        title = paper.title,
                                        courseId = paper.courseId,
                                        systemId = paper.systemId,
                                        partId = paper.partId,
                                        paperId = paper.id,
                                        subject = paper.subject,
                                        year = paper.year.toString(),
                                    ),
                                    paper.title,
                                )
                            },
                        )
                        Spacer(modifier = Modifier.height(RrbmuDimens.spacingLg))
                        LockedPreviewSection(
                            isDownloaded = detail.isDownloaded,
                            localPdfPath = detail.localPdfPath,
                            onOpenOffline = viewModel::openPdf,
                        )
                        Spacer(modifier = Modifier.height(RrbmuDimens.spacingXl))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaperHeroBanner(paper: Paper) {
    val jewel = courseJewel(paper.courseId)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(courseHeroBrush(jewel))
            .padding(20.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        CrestWatermark(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp)
                .size(150.dp)
                .graphicsLayer { alpha = 0.10f },
        )
        Column {
            Text(
                text = "PREVIOUS YEAR PAPER",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = paper.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${paper.subject}  •  ${paper.year}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun Modifier.offsetCard(): Modifier = this.offset(y = (-24).dp)

@Composable
private fun DetailCard(
    paper: Paper,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadError: String?,
    onViewPdf: () -> Unit,
    onDownloadOffline: () -> Unit,
    onShare: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(RrbmuDimens.cardRadius), spotColor = Color(0xFF0A1046))
            .clip(RoundedCornerShape(RrbmuDimens.cardRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(RrbmuDimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetadataChip(Icons.Default.Description, "Subject: ${paper.subject}", Modifier.weight(1f))
            MetadataChip(Icons.Default.DateRange, "Year: ${paper.year}", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetadataChip(Icons.Default.Description, "Code: ${paper.paperCode}", Modifier.weight(1f))
            MetadataChip(Icons.Default.School, "Course: ${paper.courseId.uppercase()}", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isDownloaded) Icons.Default.OfflinePin else Icons.Default.DownloadDone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    if (isDownloaded) "Available offline" else "Usage Stats",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    if (isDownloaded) "Saved in app vault" else "Downloaded ${paper.downloadCount} times",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        SaffronButton(
            text = "View Paper",
            onClick = onViewPdf,
            enabled = !isDownloading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            leading = {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
            },
        )

        OutlinedButton(
            onClick = onDownloadOffline,
            enabled = !isDownloading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(RrbmuDimens.buttonRadius),
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
                Text("  Downloading…")
            } else if (isDownloaded) {
                Icon(Icons.Default.OfflinePin, contentDescription = null)
                Text("  Open Offline")
            } else {
                Icon(Icons.Default.Download, contentDescription = null)
                Text("  Download Offline")
            }
        }

        OutlinedButton(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(RrbmuDimens.buttonRadius),
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Text("  Share")
        }

        AdBannerSlot()

        downloadError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun MetadataChip(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LockedPreviewSection(
    isDownloaded: Boolean,
    localPdfPath: String?,
    onOpenOffline: () -> Unit,
) {
    Column {
        Text(
            "Quick Preview",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isDownloaded && !localPdfPath.isNullOrBlank()) 320.dp else 180.dp)
                .clip(RoundedCornerShape(RrbmuDimens.cardRadius))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(RrbmuDimens.cardRadius)),
        ) {
            if (isDownloaded && !localPdfPath.isNullOrBlank()) {
                com.studies.rrbmustudies.platform.PlatformPdfViewer(
                    pdfUrl = localPdfPath,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    TextButton(onClick = onOpenOffline) {
                        Text("Open full screen", color = Color.White)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (it % 2 == 0) 0.75f else 1f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            "Download to save offline",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "You can still view online without downloading.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 24.dp, end = 24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PdfViewerScreen(
    pdfUrl: String,
    title: String,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = { RrbmuTopBar(showBack = true, onBack = onBack) },
    ) { padding ->
        com.studies.rrbmustudies.platform.PlatformPdfViewer(
            pdfUrl = pdfUrl,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}
