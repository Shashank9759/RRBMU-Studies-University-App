package com.studies.rrbmustudies.domain.usecase

import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.CourseLevel
import com.studies.rrbmustudies.domain.model.CourseSystem
import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.domain.model.Part
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.domain.repository.CourseRepository
import com.studies.rrbmustudies.domain.repository.HomeAdRepository
import com.studies.rrbmustudies.domain.repository.PaperRepository
import com.studies.rrbmustudies.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

class GetCoursesUseCase(
    private val courseRepository: CourseRepository,
) {
    operator fun invoke(level: CourseLevel? = null): Flow<List<Course>> =
        if (level != null) courseRepository.getCoursesByLevel(level)
        else courseRepository.getCourses()
}

class GetAllCoursesUseCase(
    private val courseRepository: CourseRepository,
) {
    operator fun invoke(): Flow<List<Course>> = courseRepository.getAllCourses()
}

class GetCourseUseCase(
    private val courseRepository: CourseRepository,
) {
    operator fun invoke(courseId: String): Flow<Course?> = courseRepository.getCourse(courseId)
}

class CreateCourseUseCase(
    private val courseRepository: CourseRepository,
) {
    suspend operator fun invoke(course: Course): Result<Course> = courseRepository.createCourse(course)
}

class UpdateCourseUseCase(
    private val courseRepository: CourseRepository,
) {
    suspend operator fun invoke(course: Course): Result<Course> = courseRepository.updateCourse(course)
}

class GetCourseSystemsUseCase(
    private val courseRepository: CourseRepository,
) {
    operator fun invoke(courseId: String): Flow<List<CourseSystem>> =
        courseRepository.getSystems(courseId)
}

class GetPartsUseCase(
    private val courseRepository: CourseRepository,
) {
    operator fun invoke(courseId: String, systemId: String): Flow<List<Part>> =
        courseRepository.getParts(courseId, systemId)
}

class GetPapersUseCase(
    private val paperRepository: PaperRepository,
) {
    operator fun invoke(
        courseId: String,
        systemId: String,
        partId: String,
        subjectFilter: String? = null,
        yearFilter: Int? = null,
        includeUnpublished: Boolean = false,
    ): Flow<List<Paper>> = paperRepository.getPapers(
        courseId, systemId, partId, subjectFilter, yearFilter, includeUnpublished,
    )
}

class GetPaperUseCase(
    private val paperRepository: PaperRepository,
) {
    operator fun invoke(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Flow<Paper?> = paperRepository.getPaper(courseId, systemId, partId, paperId)
}

class GetRecentPapersUseCase(
    private val paperRepository: PaperRepository,
) {
    operator fun invoke(limit: Int = 10): Flow<List<Paper>> =
        paperRepository.getRecentPapers(limit)
}

class GetCachedPapersUseCase(
    private val paperRepository: PaperRepository,
) {
    operator fun invoke(limit: Int = 8): Flow<List<Paper>> =
        paperRepository.getCachedPapers(limit)
}

class CacheViewedPaperUseCase(
    private val paperRepository: PaperRepository,
) {
    suspend operator fun invoke(paper: Paper) = paperRepository.cacheViewedPaper(paper)
}

class GetHomeAdsUseCase(
    private val homeAdRepository: HomeAdRepository,
) {
    operator fun invoke(adminMode: Boolean = false): Flow<List<HomeAd>> =
        if (adminMode) homeAdRepository.getAllAds()
        else homeAdRepository.getActiveAds()
}

class SearchPapersUseCase(
    private val searchRepository: SearchRepository,
) {
    operator fun invoke(query: String): Flow<List<Paper>> =
        searchRepository.searchPapers(query)
}

class IncrementDownloadCountUseCase(
    private val paperRepository: PaperRepository,
) {
    suspend operator fun invoke(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Result<Unit> = paperRepository.incrementDownloadCount(courseId, systemId, partId, paperId)
}

class GetNotificationsUseCase(
    private val notificationRepository: com.studies.rrbmustudies.domain.repository.NotificationRepository,
) {
    operator fun invoke(): Flow<List<com.studies.rrbmustudies.domain.model.AppNotification>> =
        notificationRepository.getNotifications()
}

class SubmitFeedbackUseCase(
    private val feedbackRepository: com.studies.rrbmustudies.domain.repository.FeedbackRepository,
) {
    suspend operator fun invoke(feedback: com.studies.rrbmustudies.domain.model.Feedback): Result<Unit> =
        feedbackRepository.submitFeedback(feedback)
}

class ObserveVaultStorageUseCase(
    private val pdfVaultRepository: com.studies.rrbmustudies.domain.repository.PdfVaultRepository,
) {
    operator fun invoke(): Flow<com.studies.rrbmustudies.domain.model.VaultStorageInfo> =
        pdfVaultRepository.observeStorageInfo()
}

class ObserveDownloadedPapersUseCase(
    private val pdfVaultRepository: com.studies.rrbmustudies.domain.repository.PdfVaultRepository,
) {
    operator fun invoke(): Flow<List<com.studies.rrbmustudies.domain.model.DownloadedPaper>> =
        pdfVaultRepository.observeDownloads()
}

class ObservePaperDownloadedUseCase(
    private val pdfVaultRepository: com.studies.rrbmustudies.domain.repository.PdfVaultRepository,
) {
    operator fun invoke(paperId: String): Flow<Boolean> =
        pdfVaultRepository.observeIsDownloaded(paperId)
}

class DownloadPaperToVaultUseCase(
    private val pdfVaultRepository: com.studies.rrbmustudies.domain.repository.PdfVaultRepository,
) {
    suspend operator fun invoke(paper: Paper): Result<com.studies.rrbmustudies.domain.model.DownloadedPaper> =
        pdfVaultRepository.downloadToVault(paper)
}

class GetVaultDownloadUseCase(
    private val pdfVaultRepository: com.studies.rrbmustudies.domain.repository.PdfVaultRepository,
) {
    suspend operator fun invoke(paperId: String): com.studies.rrbmustudies.domain.model.DownloadedPaper? =
        pdfVaultRepository.getDownload(paperId)
}

class ClearPdfVaultUseCase(
    private val pdfVaultRepository: com.studies.rrbmustudies.domain.repository.PdfVaultRepository,
) {
    suspend operator fun invoke(): Result<Unit> = pdfVaultRepository.clearVault()
}
