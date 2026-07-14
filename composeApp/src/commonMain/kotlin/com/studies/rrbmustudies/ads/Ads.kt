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

/**
 * Interstitial policy for bottom-tab switches:
 * show only after 4–8 switches AND ≥60–120s since the last full-screen ad,
 * and only once the new screen has settled — never on every switch.
 */
interface NavAdGateway {
    fun onTabSwitched(isAdmin: Boolean)
}

object NoOpNavAdGateway : NavAdGateway {
    override fun onTabSwitched(isAdmin: Boolean) = Unit
}

/** Native ad styled as an in-list card. Renders nothing until an ad is loaded. */
@Composable
expect fun NativeAdCard(modifier: Modifier = Modifier)
