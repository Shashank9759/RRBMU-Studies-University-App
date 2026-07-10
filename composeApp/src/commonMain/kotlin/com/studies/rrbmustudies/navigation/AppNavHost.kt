package com.studies.rrbmustudies.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.platform.openUrl
import com.studies.rrbmustudies.presentation.admin.AdminDashboardScreen
import com.studies.rrbmustudies.presentation.admin.AdminLoginScreen
import com.studies.rrbmustudies.presentation.admin.AdminViewModel
import com.studies.rrbmustudies.presentation.admin.ManageCoursesScreen
import com.studies.rrbmustudies.presentation.admin.ManageHomeAdsScreen
import com.studies.rrbmustudies.presentation.admin.ManageNotificationsAdminScreen
import com.studies.rrbmustudies.presentation.admin.UploadPaperScreen
import com.studies.rrbmustudies.presentation.courses.CourseDetailScreen
import com.studies.rrbmustudies.presentation.courses.CourseDetailViewModel
import com.studies.rrbmustudies.presentation.courses.PartPapersScreen
import com.studies.rrbmustudies.presentation.courses.PartPapersViewModel
import com.studies.rrbmustudies.presentation.feedback.FeedbackScreen
import com.studies.rrbmustudies.presentation.main.MainScreen
import com.studies.rrbmustudies.presentation.more.AboutTeamScreen
import com.studies.rrbmustudies.presentation.more.ContactType
import com.studies.rrbmustudies.presentation.settings.DownloadedPapersScreen
import com.studies.rrbmustudies.presentation.settings.SettingsScreen
import com.studies.rrbmustudies.presentation.more.WebViewScreen
import com.studies.rrbmustudies.presentation.notifications.NotificationDetailScreen
import com.studies.rrbmustudies.presentation.paper.PaperDetailScreen
import com.studies.rrbmustudies.presentation.paper.PaperDetailViewModel
import com.studies.rrbmustudies.presentation.paper.PdfViewerScreen
import com.studies.rrbmustudies.presentation.search.SearchScreen
import com.studies.rrbmustudies.presentation.splash.SplashScreen
import com.studies.rrbmustudies.ui.theme.AboutDefaults
import com.studies.rrbmustudies.ui.theme.LocalAdminUser
import com.studies.rrbmustudies.ui.theme.LocalIsAdmin
import com.studies.rrbmustudies.domain.model.ThemeMode
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavHost(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    initialDeepLink: IncomingDeepLink? = null,
) {
    val navController = rememberNavController()
    val adminViewModel: AdminViewModel = koinViewModel()
    val isAdmin by adminViewModel.isAdmin.collectAsStateWithLifecycle()
    val adminUser by adminViewModel.currentUser.collectAsStateWithLifecycle()
    var splashFinished by remember { mutableStateOf(false) }
    var handledDeepLinkKey by remember { mutableStateOf<String?>(null) }

    fun deepLinkKey(link: IncomingDeepLink): String = when (link) {
        is IncomingDeepLink.Notification ->
            "notification:${link.notificationId}:${link.openAttachment}"
        is IncomingDeepLink.Paper ->
            "paper:${link.courseId}/${link.systemId}/${link.partId}/${link.paperId}"
    }

    fun openDeepLink(link: IncomingDeepLink, fromSplash: Boolean) {
        if (fromSplash) {
            navController.navigate(MainRoute) {
                popUpTo(SplashRoute) { inclusive = true }
            }
        }
        when (link) {
            is IncomingDeepLink.Notification -> {
                navController.navigate(
                    NotificationDetailRoute(
                        notificationId = link.notificationId,
                        openAttachment = link.openAttachment,
                    ),
                ) { launchSingleTop = true }
            }
            is IncomingDeepLink.Paper -> {
                navController.navigate(
                    PaperDetailRoute(
                        courseId = link.courseId,
                        systemId = link.systemId,
                        partId = link.partId,
                        paperId = link.paperId,
                    ),
                ) { launchSingleTop = true }
            }
        }
    }

    // Share / tray links while app is already past splash (e.g. onNewIntent).
    LaunchedEffect(initialDeepLink, splashFinished) {
        if (!splashFinished) return@LaunchedEffect
        val link = initialDeepLink ?: return@LaunchedEffect
        val key = deepLinkKey(link)
        if (key == handledDeepLinkKey) return@LaunchedEffect
        handledDeepLinkKey = key
        openDeepLink(link, fromSplash = false)
    }

    CompositionLocalProvider(
        LocalIsAdmin provides isAdmin,
        LocalAdminUser provides adminUser,
    ) {
        NavHost(navController = navController, startDestination = SplashRoute) {
            composable<SplashRoute> {
                SplashScreen(
                    onFinished = {
                        splashFinished = true
                        val link = initialDeepLink
                        if (link != null) {
                            handledDeepLinkKey = deepLinkKey(link)
                            openDeepLink(link, fromSplash = true)
                        } else {
                            navController.navigate(MainRoute) {
                                popUpTo(SplashRoute) { inclusive = true }
                            }
                        }
                    },
                )
            }

            composable<MainRoute> {
                MainScreen(
                    isAdmin = isAdmin,
                    onCourseClick = { course ->
                        navController.navigate(
                            CourseDetailRoute(course.id, course.name, course.shortName),
                        )
                    },
                    onPaperClick = { paper ->
                        if (paper.courseId.isNotBlank() && paper.systemId.isNotBlank() && paper.partId.isNotBlank()) {
                            navController.navigate(
                                PaperDetailRoute(paper.courseId, paper.systemId, paper.partId, paper.id),
                            )
                        }
                    },
                    onSearchClick = { navController.navigate(SearchRoute) },
                    onAdClick = { ad ->
                        val url = ad.linkUrl.trim()
                        if (url.startsWith("http://", ignoreCase = true) ||
                            url.startsWith("https://", ignoreCase = true)
                        ) {
                            openUrl(url)
                        }
                    },
                    onAbout = { navController.navigate(AboutRoute) },
                    onFeedback = { navController.navigate(FeedbackRoute) },
                    onSettings = { navController.navigate(SettingsRoute) },
                    onAdminDashboard = { navController.navigate(AdminDashboardRoute) },
                    onNotificationDetail = { id ->
                        navController.navigate(NotificationDetailRoute(id))
                    },
                    onWebLink = { url, title -> navController.navigate(WebViewRoute(url, title)) },
                )
            }

            composable<CourseDetailRoute> { entry ->
                val route = entry.toRoute<CourseDetailRoute>()
                CourseDetailScreen(
                    courseShortName = route.courseShortName,
                    onBack = { navController.popBackStack() },
                    onPartClick = { systemId, partId, partName, systemType ->
                        val systemLabel = when (systemType) {
                            com.studies.rrbmustudies.domain.model.SystemType.YEARLY -> "Yearly"
                            com.studies.rrbmustudies.domain.model.SystemType.SEMESTER -> "Semester"
                            com.studies.rrbmustudies.domain.model.SystemType.ENTRANCE -> "Entrance"
                        }
                        navController.navigate(
                            PartPapersRoute(
                                courseId = route.courseId,
                                systemId = systemId,
                                partId = partId,
                                partName = partName,
                                courseShortName = route.courseShortName,
                                systemLabel = systemLabel,
                            ),
                        )
                    },
                    viewModel = koinViewModel<CourseDetailViewModel> {
                        parametersOf(route.courseId, route.courseName)
                    },
                )
            }

            composable<PartPapersRoute> { entry ->
                val route = entry.toRoute<PartPapersRoute>()
                PartPapersScreen(
                    courseShortName = route.courseShortName,
                    systemLabel = route.systemLabel,
                    onBack = { navController.popBackStack() },
                    onPaperClick = { paperId ->
                        navController.navigate(
                            PaperDetailRoute(route.courseId, route.systemId, route.partId, paperId),
                        )
                    },
                    isAdmin = isAdmin,
                    onUploadClick = {
                        navController.navigate(
                            UploadPaperRoute(route.courseId, route.systemId, route.partId),
                        )
                    },
                    viewModel = koinViewModel<PartPapersViewModel> {
                        parametersOf(route.courseId, route.systemId, route.partId, route.partName, isAdmin)
                    },
                )
            }

            composable<PaperDetailRoute> { entry ->
                val route = entry.toRoute<PaperDetailRoute>()
                PaperDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPdf = { url, title ->
                        navController.navigate(PdfViewerRoute(PdfNavCodec.encode(url), title))
                    },
                    viewModel = koinViewModel<PaperDetailViewModel> {
                        parametersOf(route.courseId, route.systemId, route.partId, route.paperId)
                    },
                )
            }

            composable<PdfViewerRoute> { entry ->
                val route = entry.toRoute<PdfViewerRoute>()
                PdfViewerScreen(
                    pdfUrl = PdfNavCodec.decode(route.encodedPdfSource),
                    title = route.title,
                    onBack = { navController.popBackStack() },
                )
            }

            composable<SearchRoute> {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onPaperClick = { paper ->
                        if (paper.courseId.isNotBlank() && paper.systemId.isNotBlank() && paper.partId.isNotBlank()) {
                            navController.navigate(
                                PaperDetailRoute(paper.courseId, paper.systemId, paper.partId, paper.id),
                            )
                        }
                    },
                    onCourseClick = { course ->
                        navController.navigate(
                            CourseDetailRoute(course.id, course.name, course.shortName),
                        )
                    },
                )
            }

            composable<AboutRoute> {
                AboutTeamScreen(
                    onBack = { navController.popBackStack() },
                    onOpenLegal = { id -> navController.navigate(LegalDocumentRoute(id)) },
                    onContactClick = { type ->
                        when (type) {
                            ContactType.WhatsApp -> openUrl("https://wa.me/${AboutDefaults.WHATSAPP_E164}")
                            ContactType.Email -> openUrl("mailto:${AboutDefaults.EMAIL}")
                            ContactType.Phone -> openUrl("tel:${AboutDefaults.PHONE_TEL}")
                        }
                    },
                )
            }

            composable<LegalDocumentRoute> { entry ->
                val route = entry.toRoute<LegalDocumentRoute>()
                com.studies.rrbmustudies.presentation.more.LegalDocumentScreen(
                    documentId = route.documentId,
                    onBack = { navController.popBackStack() },
                )
            }

            composable<FeedbackRoute> {
                FeedbackScreen(onBack = { navController.popBackStack() })
            }

            composable<SettingsRoute> {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onManageAds = { navController.navigate(ManageHomeAdsRoute) },
                    onManageNotifications = { navController.navigate(ManageNotificationsAdminRoute) },
                    onDownloadedPapers = { navController.navigate(DownloadedPapersRoute) },
                )
            }

            composable<DownloadedPapersRoute> {
                DownloadedPapersScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPdf = { path, title ->
                        navController.navigate(PdfViewerRoute(PdfNavCodec.encode(path), title))
                    },
                )
            }

            composable<NotificationDetailRoute> { entry ->
                val route = entry.toRoute<NotificationDetailRoute>()
                NotificationDetailScreen(
                    notificationId = route.notificationId,
                    openAttachment = route.openAttachment,
                    onBack = { navController.popBackStack() },
                    onOpenPdf = { url, title ->
                        navController.navigate(PdfViewerRoute(PdfNavCodec.encode(url), title))
                    },
                    onViewPaperList = {
                        navController.popBackStack()
                    },
                )
            }

            composable<AdminLoginRoute> {
                AdminLoginScreen(
                    onBack = { navController.popBackStack() },
                    onLoginSuccess = {
                        navController.navigate(AdminDashboardRoute) {
                            popUpTo(AdminLoginRoute) { inclusive = true }
                        }
                    },
                )
            }

            composable<AdminDashboardRoute> {
                AdminDashboardScreen(
                    onBack = { navController.popBackStack() },
                    onManageCourses = { navController.navigate(ManageCoursesAdminRoute) },
                    onManageAds = { navController.navigate(ManageHomeAdsRoute) },
                    onManageNotifications = { navController.navigate(ManageNotificationsAdminRoute) },
                    onUploadPaper = { navController.navigate(UploadPaperRoute()) },
                    onSignOut = {
                        adminViewModel.signOut()
                        navController.popBackStack()
                    },
                    adminEmail = adminUser?.email,
                )
            }

            composable<ManageCoursesAdminRoute> {
                ManageCoursesScreen(onBack = { navController.popBackStack() })
            }

            composable<ManageHomeAdsRoute> {
                ManageHomeAdsScreen(onBack = { navController.popBackStack() })
            }

            composable<ManageNotificationsAdminRoute> {
                ManageNotificationsAdminScreen(onBack = { navController.popBackStack() })
            }

            composable<UploadPaperRoute> { entry ->
                val route = entry.toRoute<UploadPaperRoute>()
                UploadPaperScreen(
                    onBack = { navController.popBackStack() },
                    onUploadSuccess = { navController.popBackStack() },
                    viewModel = koinViewModel<com.studies.rrbmustudies.presentation.admin.UploadPaperViewModel> {
                        parametersOf(route.courseId, route.systemId, route.partId)
                    },
                )
            }

            composable<WebViewRoute> { entry ->
                val route = entry.toRoute<WebViewRoute>()
                WebViewScreen(
                    title = route.title,
                    url = route.url,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
