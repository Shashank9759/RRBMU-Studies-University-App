package com.studies.rrbmustudies.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import com.studies.rrbmustudies.data.local.RrbmuDatabase
import org.koin.dsl.module

actual val platformModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = RrbmuDatabase.Schema,
            context = get<Context>(),
            // v2: adds DownloadedPaper vault table (fresh schema for local installs)
            name = "rrbmu_v2.db",
        )
    }
    single { RrbmuDatabase(get()) }
    single<ObservableSettings> {
        SharedPreferencesSettings(
            get<Context>().getSharedPreferences("rrbmu_prefs", Context.MODE_PRIVATE),
        )
    }
    single<com.studies.rrbmustudies.data.platform.PdfVaultFileStore> {
        com.studies.rrbmustudies.data.platform.AndroidPdfVaultFileStore(get())
    }
}
