package com.studies.rrbmustudies.domain.repository

import com.studies.rrbmustudies.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<AppNotification>>
    fun getAllNotifications(): Flow<List<AppNotification>>
    suspend fun createNotification(notification: AppNotification): Result<AppNotification>
    suspend fun updateNotification(notification: AppNotification): Result<AppNotification>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    /** Upload optional PDF/image attachment; returns download URL. */
    suspend fun uploadNotificationAttachment(bytes: ByteArray, fileName: String): Result<String>
}
