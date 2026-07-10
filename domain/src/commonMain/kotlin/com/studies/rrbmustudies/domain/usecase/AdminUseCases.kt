package com.studies.rrbmustudies.domain.usecase

import com.studies.rrbmustudies.domain.model.AdminUser
import com.studies.rrbmustudies.domain.model.AppNotification
import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.domain.repository.AuthRepository
import com.studies.rrbmustudies.domain.repository.HomeAdRepository
import com.studies.rrbmustudies.domain.repository.NotificationRepository
import com.studies.rrbmustudies.domain.repository.PaperRepository
import kotlinx.coroutines.flow.Flow

class ObserveAdminStateUseCase(
    private val authRepository: AuthRepository,
) {
    val isAdmin: Flow<Boolean> = authRepository.isAdmin
    val currentUser: Flow<AdminUser?> = authRepository.currentUser
}

class SignInAdminUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<AdminUser> =
        authRepository.signIn(email, password)
}

class SignOutAdminUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.signOut()
}

class CreateHomeAdUseCase(
    private val homeAdRepository: HomeAdRepository,
) {
    suspend operator fun invoke(ad: HomeAd): Result<HomeAd> = homeAdRepository.createAd(ad)
}

class UpdateHomeAdUseCase(
    private val homeAdRepository: HomeAdRepository,
) {
    suspend operator fun invoke(ad: HomeAd): Result<HomeAd> = homeAdRepository.updateAd(ad)
}

class DeleteHomeAdUseCase(
    private val homeAdRepository: HomeAdRepository,
) {
    suspend operator fun invoke(adId: String): Result<Unit> = homeAdRepository.deleteAd(adId)
}

class UploadHomeAdImageUseCase(
    private val homeAdRepository: HomeAdRepository,
) {
    suspend operator fun invoke(bytes: ByteArray, fileName: String): Result<String> =
        homeAdRepository.uploadAdImage(bytes, fileName)
}

class ObserveHomeCarouselSettingsUseCase(
    private val homeAdRepository: HomeAdRepository,
) {
    operator fun invoke() = homeAdRepository.observeCarouselSettings()
}

class SaveHomeCarouselSettingsUseCase(
    private val homeAdRepository: HomeAdRepository,
) {
    suspend operator fun invoke(settings: com.studies.rrbmustudies.domain.model.HomeCarouselSettings): Result<Unit> =
        homeAdRepository.saveCarouselSettings(settings)
}

class ReorderHomeAdsUseCase(
    private val homeAdRepository: HomeAdRepository,
) {
    suspend operator fun invoke(orderedAds: List<HomeAd>): Result<Unit> =
        homeAdRepository.reorderAds(orderedAds)
}

class UploadCourseBackgroundUseCase(
    private val courseRepository: com.studies.rrbmustudies.domain.repository.CourseRepository,
) {
    suspend operator fun invoke(bytes: ByteArray, fileName: String, courseId: String): Result<String> =
        courseRepository.uploadCourseBackground(bytes, fileName, courseId)
}

class GetAllNotificationsUseCase(
    private val notificationRepository: NotificationRepository,
) {
    operator fun invoke(): Flow<List<AppNotification>> = notificationRepository.getAllNotifications()
}

class CreateNotificationUseCase(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(notification: AppNotification): Result<AppNotification> =
        notificationRepository.createNotification(notification)
}

class UpdateNotificationUseCase(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(notification: AppNotification): Result<AppNotification> =
        notificationRepository.updateNotification(notification)
}

class DeleteNotificationUseCase(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(notificationId: String): Result<Unit> =
        notificationRepository.deleteNotification(notificationId)
}

class UploadNotificationAttachmentUseCase(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(bytes: ByteArray, fileName: String): Result<String> =
        notificationRepository.uploadNotificationAttachment(bytes, fileName)
}

class UploadPaperUseCase(
    private val paperRepository: PaperRepository,
) {
    suspend operator fun invoke(paper: Paper, pdfBytes: ByteArray): Result<Paper> =
        paperRepository.uploadPaper(paper, pdfBytes)
}

class UpdatePaperUseCase(
    private val paperRepository: PaperRepository,
) {
    suspend operator fun invoke(paper: Paper): Result<Paper> = paperRepository.updatePaper(paper)
}

class DeletePaperUseCase(
    private val paperRepository: PaperRepository,
) {
    suspend operator fun invoke(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Result<Unit> = paperRepository.deletePaper(courseId, systemId, partId, paperId)
}
