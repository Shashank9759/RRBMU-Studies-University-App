package com.studies.rrbmustudies.ads

import com.studies.rrbmustudies.BuildConfig

/**
 * Ad unit IDs are supplied via [BuildConfig] fields set in build.gradle.kts.
 * Debug builds always resolve to Google's official test IDs; release builds use the
 * real IDs from local.properties (falling back to test IDs when absent).
 */
object AdUnitIds {
    val BANNER: String = BuildConfig.ADMOB_BANNER
    val INTERSTITIAL: String = BuildConfig.ADMOB_INTERSTITIAL
    val REWARDED: String = BuildConfig.ADMOB_REWARDED
    val REWARDED_INTERSTITIAL: String = BuildConfig.ADMOB_REWARDED_INTERSTITIAL
}
