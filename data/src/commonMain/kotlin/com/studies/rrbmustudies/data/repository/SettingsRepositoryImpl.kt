package com.studies.rrbmustudies.data.repository

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.studies.rrbmustudies.domain.model.ThemeMode
import com.studies.rrbmustudies.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val settings: ObservableSettings,
) : SettingsRepository {

    private val flowSettings: FlowSettings = settings.toFlowSettings()

    override val themeMode: Flow<ThemeMode> =
        flowSettings.getStringFlow(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
            .map { name -> ThemeMode.entries.find { it.name == name } ?: ThemeMode.SYSTEM }

    override suspend fun setThemeMode(mode: ThemeMode) {
        flowSettings.putString(KEY_THEME_MODE, mode.name)
    }

    override val readNotificationIds: Flow<Set<String>> =
        flowSettings.getStringFlow(KEY_READ_NOTIFICATIONS, "")
            .map { raw ->
                raw.split(',')
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toSet()
            }

    override suspend fun markNotificationRead(id: String) {
        if (id.isBlank()) return
        val current = settings.getString(KEY_READ_NOTIFICATIONS, "")
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toMutableSet()
        if (current.add(id)) {
            // Cap growth so prefs stay small.
            val trimmed = current.toList().takeLast(300)
            settings.putString(KEY_READ_NOTIFICATIONS, trimmed.joinToString(","))
        }
    }

    override suspend fun getPaperOpenCount(): Int =
        settings.getInt(KEY_PAPER_OPEN_COUNT, 0)

    override suspend fun incrementPaperOpenCount(): Int {
        val next = getPaperOpenCount() + 1
        settings.putInt(KEY_PAPER_OPEN_COUNT, next)
        return next
    }

    override suspend fun getLastFullScreenAdAt(): Long =
        settings.getLong(KEY_LAST_FULL_SCREEN_AD_AT, 0L)

    override suspend fun setLastFullScreenAdAt(timestamp: Long) {
        settings.putLong(KEY_LAST_FULL_SCREEN_AD_AT, timestamp)
    }

    override suspend fun getSkipNextInterstitial(): Boolean =
        settings.getBoolean(KEY_SKIP_NEXT_INTERSTITIAL, false)

    override suspend fun setSkipNextInterstitial(skip: Boolean) {
        settings.putBoolean(KEY_SKIP_NEXT_INTERSTITIAL, skip)
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_READ_NOTIFICATIONS = "read_notification_ids"
        const val KEY_PAPER_OPEN_COUNT = "paper_open_count"
        const val KEY_LAST_FULL_SCREEN_AD_AT = "last_full_screen_ad_at"
        const val KEY_SKIP_NEXT_INTERSTITIAL = "skip_next_interstitial"
    }
}
