package com.studies.rrbmustudies.di

import com.studies.rrbmustudies.ads.AndroidAdManager
import com.studies.rrbmustudies.ads.AndroidNavAdGateway
import com.studies.rrbmustudies.ads.AndroidPaperAdGateway
import com.studies.rrbmustudies.ads.NavAdGateway
import com.studies.rrbmustudies.ads.PaperAdGateway
import com.studies.rrbmustudies.platform.currentActivity
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidAdsModule = module {
    single {
        AndroidAdManager(androidContext()).also { it.initialize() }
    }
    single<PaperAdGateway> {
        AndroidPaperAdGateway(
            settingsRepository = get(),
            adManager = get(),
            activityProvider = { currentActivity() },
        )
    }
    single<NavAdGateway> {
        AndroidNavAdGateway(
            adManager = get(),
            settingsRepository = get(),
            activityProvider = { currentActivity() },
        )
    }
}
