package com.studies.rrbmustudies.ads

import android.app.Activity
import com.studies.rrbmustudies.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Bottom-navigation interstitial policy:
 *  - fires only after a randomized 4–8 tab switches, AND
 *  - only when 60–120s (randomized per cycle) have passed since the LAST
 *    full-screen ad anywhere in the app (shared via SettingsRepository), AND
 *  - only after the new screen has had a moment to settle (short delay),
 *    so the ad never interrupts mid-action.
 */
class AndroidNavAdGateway(
    private val adManager: AndroidAdManager,
    private val settingsRepository: SettingsRepository,
    private val activityProvider: () -> Activity?,
) : NavAdGateway {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var switchCount = 0
    private var nextThreshold = Random.nextInt(MIN_SWITCHES, MAX_SWITCHES + 1)
    private var minGapMs = Random.nextLong(MIN_GAP_MS, MAX_GAP_MS + 1)
    private var showing = false

    override fun onTabSwitched(isAdmin: Boolean) {
        if (isAdmin || showing) return
        switchCount++
        if (switchCount < nextThreshold) return

        scope.launch {
            val now = System.currentTimeMillis()
            val lastAd = settingsRepository.getLastFullScreenAdAt()
            if (now - lastAd < minGapMs) return@launch

            // Let the destination screen finish its entrance before covering it.
            delay(SETTLE_DELAY_MS)
            val activity = activityProvider() ?: return@launch

            showing = true
            val shown = try {
                adManager.showInterstitial(activity)
            } finally {
                showing = false
            }
            if (shown) {
                settingsRepository.setLastFullScreenAdAt(System.currentTimeMillis())
                switchCount = 0
                nextThreshold = Random.nextInt(MIN_SWITCHES, MAX_SWITCHES + 1)
                minGapMs = Random.nextLong(MIN_GAP_MS, MAX_GAP_MS + 1)
            }
        }
    }

    private companion object {
        const val MIN_SWITCHES = 4
        const val MAX_SWITCHES = 8
        const val MIN_GAP_MS = 60_000L
        const val MAX_GAP_MS = 120_000L
        const val SETTLE_DELAY_MS = 700L
    }
}
