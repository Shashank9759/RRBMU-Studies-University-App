package com.studies.rrbmustudies.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Serializable
data object MainRoute

@Serializable
data class CourseDetailRoute(
    val courseId: String,
    val courseName: String = "",
    val courseShortName: String = "",
)

@Serializable
data class PartPapersRoute(
    val courseId: String,
    val systemId: String,
    val partId: String,
    val partName: String = "",
    val courseShortName: String = "",
    val systemLabel: String = "Yearly",
)

@Serializable
data class PaperDetailRoute(
    val courseId: String,
    val systemId: String,
    val partId: String,
    val paperId: String,
)

@Serializable
data object SearchRoute

@Serializable
data class PdfViewerRoute(
    /** Base64 url-safe encoded path or https URL (see [PdfNavCodec]). */
    val encodedPdfSource: String,
    val title: String = "",
)

@Serializable
data class WebViewRoute(
    val url: String,
    val title: String = "",
)

@Serializable
data object AboutRoute

@Serializable
data class LegalDocumentRoute(
    val documentId: String,
)

@Serializable
data object FeedbackRoute

@Serializable
data object SettingsRoute

@Serializable
data object DownloadedPapersRoute

@Serializable
data class NotificationDetailRoute(
    val notificationId: String,
    val openAttachment: Boolean = false,
)

@Serializable
data object AdminLoginRoute

@Serializable
data object AdminDashboardRoute

@Serializable
data object ManageHomeAdsRoute

@Serializable
data object ManageNotificationsAdminRoute

@Serializable
data object ManageCoursesAdminRoute

@Serializable
data class UploadPaperRoute(
    val courseId: String = "",
    val systemId: String = "",
    val partId: String = "",
)

enum class BottomTab {
    Home,
    Courses,
    Notifications,
    More,
}
