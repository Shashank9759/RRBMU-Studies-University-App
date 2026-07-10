package com.studies.rrbmustudies.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.domain.model.DownloadedPaper
import com.studies.rrbmustudies.ui.components.EmptyState
import com.studies.rrbmustudies.ui.components.RrbmuTopBar
import com.studies.rrbmustudies.ui.components.AdBannerSlot
import com.studies.rrbmustudies.ui.components.SettingsCard
import com.studies.rrbmustudies.ui.components.SettingsRow
import com.studies.rrbmustudies.ui.theme.RrbmuDimens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DownloadedPapersScreen(
    onBack: () -> Unit,
    onOpenPdf: (String, String) -> Unit,
    viewModel: DownloadedPapersViewModel = koinViewModel(),
) {
    val papers by viewModel.papers.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            RrbmuTopBar(
                showBack = true,
                onBack = onBack,
            )
        },
    ) { padding ->
        if (papers.isEmpty()) {
            EmptyState(
                title = "No offline papers",
                message = "Download a paper from Paper Detail to save it in the private app vault.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = RrbmuDimens.screenHorizontal,
                    vertical = RrbmuDimens.spacingMd,
                ),
                verticalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingSm),
            ) {
                item {
                    Text(
                        text = "${papers.size} paper${if (papers.size == 1) "" else "s"} saved offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                items(papers, key = { it.id }) { paper ->
                    DownloadedPaperRow(
                        paper = paper,
                        onClick = { onOpenPdf(paper.localPath, paper.title) },
                    )
                }
                item { AdBannerSlot() }
            }
        }
    }
}

@Composable
private fun DownloadedPaperRow(
    paper: DownloadedPaper,
    onClick: () -> Unit,
) {
    SettingsCard {
        SettingsRow(
            title = paper.title,
            subtitle = "${paper.subject} · ${paper.year}",
            icon = Icons.Default.Description,
            onClick = onClick,
            trailing = {
                Text(
                    text = formatBytes(paper.fileSizeBytes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        )
    }
}

private fun formatBytes(bytes: Long): String =
    when {
        bytes <= 0L -> "—"
        bytes < 1024L * 1024L -> "${(bytes + 512) / 1024} KB"
        else -> {
            val mb = bytes.toDouble() / (1024.0 * 1024.0)
            val rounded = (mb * 10.0).toInt() / 10.0
            "$rounded MB"
        }
    }
