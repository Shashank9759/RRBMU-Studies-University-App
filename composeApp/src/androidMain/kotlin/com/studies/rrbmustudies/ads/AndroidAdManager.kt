package com.studies.rrbmustudies.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.studies.rrbmustudies.domain.repository.SettingsRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidAdManager(
    private val context: Context,
) {
    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null
    private var rewardedInterstitial: RewardedInterstitialAd? = null

    fun initialize() {
        MobileAds.initialize(context)
        preloadAll()
    }

    fun preloadAll() {
        loadInterstitial()
        loadRewarded()
        loadRewardedInterstitial()
    }

    private fun loadInterstitial() {
        InterstitialAd.load(
            context,
            AdUnitIds.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                }
            },
        )
    }

    private fun loadRewarded() {
        RewardedAd.load(
            context,
            AdUnitIds.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewarded = null
                }
            },
        )
    }

    private fun loadRewardedInterstitial() {
        RewardedInterstitialAd.load(
            context,
            AdUnitIds.REWARDED_INTERSTITIAL,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitial = null
                }
            },
        )
    }

    suspend fun showInterstitial(activity: Activity): Boolean = suspendCancellableCoroutine { cont ->
        val ad = interstitial
        if (ad == null) {
            loadInterstitial()
            cont.resume(false)
            return@suspendCancellableCoroutine
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                loadInterstitial()
                if (cont.isActive) cont.resume(true)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitial = null
                loadInterstitial()
                if (cont.isActive) cont.resume(false)
            }
        }
        ad.show(activity)
    }

    suspend fun showRewarded(activity: Activity): Boolean = suspendCancellableCoroutine { cont ->
        val ad = rewarded
        if (ad == null) {
            loadRewarded()
            cont.resume(false)
            return@suspendCancellableCoroutine
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null
                loadRewarded()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewarded = null
                loadRewarded()
                if (cont.isActive) cont.resume(false)
            }
        }
        ad.show(activity) {
            if (cont.isActive) cont.resume(true)
        }
    }

    suspend fun showRewardedInterstitial(activity: Activity): Boolean = suspendCancellableCoroutine { cont ->
        val ad = rewardedInterstitial
        if (ad == null) {
            loadRewardedInterstitial()
            cont.resume(false)
            return@suspendCancellableCoroutine
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedInterstitial = null
                loadRewardedInterstitial()
                if (cont.isActive) cont.resume(true)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedInterstitial = null
                loadRewardedInterstitial()
                if (cont.isActive) cont.resume(false)
            }
        }
        ad.show(activity) {
            if (cont.isActive) cont.resume(true)
        }
    }
}

class AndroidPaperAdGateway(
    private val settingsRepository: SettingsRepository,
    private val adManager: AndroidAdManager,
    private val activityProvider: () -> Activity?,
) : PaperAdGateway {

    override suspend fun requestBeforeOpenPaper(isAdmin: Boolean, onProceed: () -> Unit) {
        if (isAdmin) {
            onProceed()
            return
        }
        val count = settingsRepository.incrementPaperOpenCount()
        val now = System.currentTimeMillis()
        val lastAd = settingsRepository.getLastFullScreenAdAt()
        val canShowFullScreen = now - lastAd >= MIN_FULL_SCREEN_GAP_MS

        if (settingsRepository.getSkipNextInterstitial()) {
            settingsRepository.setSkipNextInterstitial(false)
            onProceed()
            return
        }

        val activity = activityProvider() ?: run {
            onProceed()
            return
        }

        when {
            count % 9 == 0 && canShowFullScreen -> {
                val shown = adManager.showRewardedInterstitial(activity)
                if (shown) settingsRepository.setLastFullScreenAdAt(now)
                onProceed()
            }
            count % 5 == 0 && canShowFullScreen -> {
                val shown = adManager.showRewarded(activity)
                if (shown) {
                    settingsRepository.setLastFullScreenAdAt(now)
                    settingsRepository.setSkipNextInterstitial(true)
                }
                onProceed()
            }
            count % 2 == 0 && canShowFullScreen -> {
                val shown = adManager.showInterstitial(activity)
                if (shown) settingsRepository.setLastFullScreenAdAt(now)
                onProceed()
            }
            else -> onProceed()
        }
    }

    override suspend fun requestBeforeOfflineDownload(isAdmin: Boolean, onProceed: () -> Unit) {
        if (isAdmin) {
            onProceed()
            return
        }
        val count = settingsRepository.getPaperOpenCount()
        if (count % 5 != 0) {
            onProceed()
            return
        }
        val activity = activityProvider() ?: run {
            onProceed()
            return
        }
        val shown = adManager.showRewarded(activity)
        if (shown) {
            settingsRepository.setLastFullScreenAdAt(System.currentTimeMillis())
            settingsRepository.setSkipNextInterstitial(true)
        }
        onProceed()
    }

    private companion object {
        const val MIN_FULL_SCREEN_GAP_MS = 90_000L
    }
}
