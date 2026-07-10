package com.studies.rrbmustudies.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect fun initializePlatform()

expect fun openUrl(url: String)

expect fun shareText(text: String, title: String = "Share")

@Composable
expect fun PlatformWebView(
    url: String,
    modifier: Modifier = Modifier,
)

@Composable
expect fun PlatformPdfViewer(
    pdfUrl: String,
    modifier: Modifier = Modifier,
)
