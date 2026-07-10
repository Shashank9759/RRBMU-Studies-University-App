package com.studies.rrbmustudies.data.remote

import com.studies.rrbmustudies.data.dto.AdminDto
import com.studies.rrbmustudies.data.dto.FeedbackDto
import com.studies.rrbmustudies.data.dto.HomeAdDto
import com.studies.rrbmustudies.data.dto.HomeCarouselSettingsDto
import com.studies.rrbmustudies.data.dto.NotificationDto
import kotlinx.coroutines.flow.Flow

interface HomeAdRemoteDataSource {
    fun observeActiveAds(): Flow<List<Pair<String, HomeAdDto>>>
    fun observeAllAds(): Flow<List<Pair<String, HomeAdDto>>>
    fun observeCarouselSettings(): Flow<HomeCarouselSettingsDto>
    suspend fun saveCarouselSettings(settings: HomeCarouselSettingsDto)
    suspend fun saveAd(id: String, ad: HomeAdDto)
    suspend fun deleteAd(id: String)
}

interface NotificationRemoteDataSource {
    fun observeNotifications(): Flow<List<Pair<String, NotificationDto>>>
    fun observeAllNotifications(): Flow<List<Pair<String, NotificationDto>>>
    suspend fun saveNotification(id: String, notification: NotificationDto)
    suspend fun deleteNotification(id: String)
}

interface FeedbackRemoteDataSource {
    suspend fun submitFeedback(id: String, feedback: FeedbackDto)
}

interface AuthRemoteDataSource {
    val currentUid: Flow<String?>
    suspend fun signIn(email: String, password: String): String
    suspend fun signOut()
    suspend fun isAdmin(uid: String): Boolean
    suspend fun getAdminProfile(uid: String): AdminDto?
}
