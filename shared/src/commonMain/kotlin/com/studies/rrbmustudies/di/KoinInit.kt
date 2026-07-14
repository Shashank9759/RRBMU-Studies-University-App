package com.studies.rrbmustudies.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

fun initKoin(
    extraModules: List<org.koin.core.module.Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
) {
    // The Android activity re-runs this on every recreation (e.g. system
    // dark/light switch). Koin is process-wide, so starting twice crashes.
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return

    startKoin {
        allowOverride(true)
        appDeclaration()
        modules(listOf(dataModule, domainModule, platformModule) + extraModules)
    }
}
