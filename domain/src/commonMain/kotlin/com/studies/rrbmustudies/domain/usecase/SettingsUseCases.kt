package com.studies.rrbmustudies.domain.usecase

import com.studies.rrbmustudies.domain.model.ThemeMode
import com.studies.rrbmustudies.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveThemeModeUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<ThemeMode> = settingsRepository.themeMode
}

class SetThemeModeUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(mode: ThemeMode) = settingsRepository.setThemeMode(mode)
}
