package com.studies.rrbmustudies.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import com.studies.rrbmustudies.data.local.RrbmuDatabase
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformModule = module {
    single<SqlDriver> {
        NativeSqliteDriver(RrbmuDatabase.Schema, "rrbmu_v2.db")
    }
    single { RrbmuDatabase(get()) }
    single<ObservableSettings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
    single<com.studies.rrbmustudies.data.platform.PdfVaultFileStore> {
        com.studies.rrbmustudies.data.platform.createPdfVaultFileStore()
    }
}
