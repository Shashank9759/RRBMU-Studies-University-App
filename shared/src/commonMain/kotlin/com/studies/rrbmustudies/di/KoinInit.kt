package com.studies.rrbmustudies.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    extraModules: List<org.koin.core.module.Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
) {
    startKoin {
        allowOverride(true)
        appDeclaration()
        modules(listOf(dataModule, domainModule, platformModule) + extraModules)
    }
}
