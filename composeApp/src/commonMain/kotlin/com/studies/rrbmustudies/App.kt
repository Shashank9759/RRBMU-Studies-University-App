package com.studies.rrbmustudies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.navigation.AppNavHost
import com.studies.rrbmustudies.navigation.IncomingDeepLink
import com.studies.rrbmustudies.presentation.settings.AppSettingsViewModel
import com.studies.rrbmustudies.ui.theme.RrbmuTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    initialDeepLink: IncomingDeepLink? = null,
) {
    val settingsViewModel: AppSettingsViewModel = koinViewModel()
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

    RrbmuTheme(themeMode = themeMode) {
        com.studies.rrbmustudies.platform.RequestNotificationPermission()
        AppNavHost(
            themeMode = themeMode,
            onThemeModeChange = settingsViewModel::setThemeMode,
            initialDeepLink = initialDeepLink,
        )
    }
}
