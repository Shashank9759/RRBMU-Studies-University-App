package com.studies.rrbmustudies.domain.repository

import com.studies.rrbmustudies.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    val readNotificationIds: Flow<Set<String>>
    suspend fun markNotificationRead(id: String)

    suspend fun getPaperOpenCount(): Int
    suspend fun incrementPaperOpenCount(): Int
    suspend fun getLastFullScreenAdAt(): Long
    suspend fun setLastFullScreenAdAt(timestamp: Long)
    suspend fun getSkipNextInterstitial(): Boolean
    suspend fun setSkipNextInterstitial(skip: Boolean)
}
