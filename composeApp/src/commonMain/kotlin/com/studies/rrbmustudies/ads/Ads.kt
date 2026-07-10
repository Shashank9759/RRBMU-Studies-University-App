package com.studies.rrbmustudies.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String? = null,
)

interface PaperAdGateway {
    suspend fun requestBeforeOpenPaper(isAdmin: Boolean, onProceed: () -> Unit)
    suspend fun requestBeforeOfflineDownload(isAdmin: Boolean, onProceed: () -> Unit)
}

object NoOpPaperAdGateway : PaperAdGateway {
    override suspend fun requestBeforeOpenPaper(isAdmin: Boolean, onProceed: () -> Unit) = onProceed()
    override suspend fun requestBeforeOfflineDownload(isAdmin: Boolean, onProceed: () -> Unit) = onProceed()
}
