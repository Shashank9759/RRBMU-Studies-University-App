package com.studies.rrbmustudies.data.repository

import com.studies.rrbmustudies.data.mapper.toDomain
import com.studies.rrbmustudies.data.mapper.toDto
import com.studies.rrbmustudies.data.remote.FeedbackRemoteDataSource
import com.studies.rrbmustudies.data.remote.HomeAdRemoteDataSource
import com.studies.rrbmustudies.data.remote.NotificationRemoteDataSource
import com.studies.rrbmustudies.data.remote.StorageRemoteDataSource
import com.studies.rrbmustudies.domain.model.AppNotification
import com.studies.rrbmustudies.domain.model.Feedback
import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.domain.model.HomeCarouselSettings
import com.studies.rrbmustudies.domain.model.HomeDefaults
import com.studies.rrbmustudies.domain.repository.FeedbackRepository
import com.studies.rrbmustudies.domain.repository.HomeAdRepository
import com.studies.rrbmustudies.domain.repository.NotificationRepository
import com.studies.rrbmustudies.platform.currentTimeMillis
import com.studies.rrbmustudies.platform.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class HomeAdRepositoryImpl(
    private val remote: HomeAdRemoteDataSource,
    private val storage: StorageRemoteDataSource,
) : HomeAdRepository {

    override fun getActiveAds(): Flow<List<HomeAd>> =
        remote.observeActiveAds()
            .map { list -> list.map { (id, dto) -> dto.toDomain(id) } }
            .map { ads -> ads.ifEmpty { HomeDefaults.promoAds } }
            .catch { emit(HomeDefaults.promoAds) }

    override fun getAllAds(): Flow<List<HomeAd>> =
        remote.observeAllAds()
            .map { list -> list.map { (id, dto) -> dto.toDomain(id) } }
            .catch { emit(HomeDefaults.promoAds) }

    override fun observeCarouselSettings(): Flow<HomeCarouselSettings> =
        remote.observeCarouselSettings()
            .map { dto -> HomeCarouselSettings(slideIntervalSeconds = dto.slideIntervalSeconds.coerceIn(2, 60)) }
            .catch { emit(HomeCarouselSettings()) }

    override suspend fun saveCarouselSettings(settings: HomeCarouselSettings): Result<Unit> = runCatching {
        remote.saveCarouselSettings(
            com.studies.rrbmustudies.data.dto.HomeCarouselSettingsDto(
                slideIntervalSeconds = settings.slideIntervalSeconds.coerceIn(2, 60),
            ),
        )
    }

    override suspend fun createAd(ad: HomeAd): Result<HomeAd> = runCatching {
        val id = ad.id.ifBlank { generateId() }
        val created = ad.copy(id = id, createdAt = currentTimeMillis())
        remote.saveAd(id, created.toDto())
        created
    }

    override suspend fun updateAd(ad: HomeAd): Result<HomeAd> = runCatching {
        remote.saveAd(ad.id, ad.toDto())
        ad
    }

    override suspend fun reorderAds(orderedAds: List<HomeAd>): Result<Unit> = runCatching {
        orderedAds.forEachIndexed { index, ad ->
            remote.saveAd(ad.id, ad.copy(order = index).toDto())
        }
    }

    override suspend fun deleteAd(adId: String): Result<Unit> = runCatching {
        remote.deleteAd(adId)
    }

    override suspend fun uploadAdImage(bytes: ByteArray, fileName: String): Result<String> =
        runCatching {
            require(bytes.isNotEmpty()) { "Image is empty" }
            val ext = fileName.substringAfterLast('.', "jpg").lowercase().ifBlank { "jpg" }
            val contentType = when (ext) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                "gif" -> "image/gif"
                else -> "image/jpeg"
            }
            val path = "ads/${generateId()}.$ext"
            storage.uploadFile(path, bytes, contentType)
        }
}

class NotificationRepositoryImpl(
    private val remote: NotificationRemoteDataSource,
    private val storage: StorageRemoteDataSource,
) : NotificationRepository {

    override fun getNotifications(): Flow<List<AppNotification>> =
        remote.observeNotifications().map { list ->
            list.map { (id, dto) -> dto.toDomain(id) }
        }

    override fun getAllNotifications(): Flow<List<AppNotification>> =
        remote.observeAllNotifications().map { list ->
            list.map { (id, dto) -> dto.toDomain(id) }
        }

    override suspend fun createNotification(notification: AppNotification): Result<AppNotification> =
        runCatching {
            val id = notification.id.ifBlank { generateId() }
            val created = notification.copy(id = id, createdAt = currentTimeMillis())
            remote.saveNotification(id, created.toDto())
            created
        }

    override suspend fun updateNotification(notification: AppNotification): Result<AppNotification> =
        runCatching {
            remote.saveNotification(notification.id, notification.toDto())
            notification
        }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> = runCatching {
        remote.deleteNotification(notificationId)
    }

    override suspend fun uploadNotificationAttachment(
        bytes: ByteArray,
        fileName: String,
    ): Result<String> = runCatching {
        require(bytes.isNotEmpty()) { "File is empty" }
        val ext = fileName.substringAfterLast('.', "bin").lowercase().ifBlank { "bin" }
        val contentType = when (ext) {
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "jpg", "jpeg" -> "image/jpeg"
            else -> "application/octet-stream"
        }
        storage.uploadFile("notification_attachments/${generateId()}.$ext", bytes, contentType)
    }
}

class FeedbackRepositoryImpl(
    private val remote: FeedbackRemoteDataSource,
) : FeedbackRepository {

    override suspend fun submitFeedback(feedback: Feedback): Result<Unit> = runCatching {
        val id = feedback.id.ifBlank { generateId() }
        val dto = feedback.copy(id = id, createdAt = currentTimeMillis()).toDto()
        remote.submitFeedback(id, dto)
    }
}
