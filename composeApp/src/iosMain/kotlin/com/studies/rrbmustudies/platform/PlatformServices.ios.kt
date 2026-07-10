package com.studies.rrbmustudies.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

actual fun initializePlatform() = Unit

actual fun openUrl(url: String) = Unit

actual fun shareText(text: String, title: String) = Unit

@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("WebView not available on this platform yet.\n$url")
    }
}

@Composable
actual fun PlatformPdfViewer(
    pdfUrl: String,
    modifier: Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("PDF viewer not available on this platform yet.")
    }
}
