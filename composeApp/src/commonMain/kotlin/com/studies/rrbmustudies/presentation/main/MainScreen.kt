package com.studies.rrbmustudies.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.domain.model.AppNotification
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.navigation.BottomTab
import com.studies.rrbmustudies.presentation.courses.CoursesScreen
import com.studies.rrbmustudies.presentation.home.HomeScreen
import com.studies.rrbmustudies.presentation.more.MoreScreen
import com.studies.rrbmustudies.presentation.notifications.NotificationsScreen
import com.studies.rrbmustudies.presentation.notifications.NotificationsViewModel
import com.studies.rrbmustudies.ui.components.RrbmuBottomBar
import com.studies.rrbmustudies.ui.components.RrbmuTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    isAdmin: Boolean,
    onCourseClick: (Course) -> Unit,
    onPaperClick: (Paper) -> Unit,
    onSearchClick: () -> Unit,
    onAdClick: (HomeAd) -> Unit,
    onAbout: () -> Unit,
    onFeedback: () -> Unit,
    onSettings: () -> Unit,
    onAdminDashboard: () -> Unit,
    onNotificationDetail: (String) -> Unit,
    onWebLink: (String, String) -> Unit,
    initialTab: BottomTab = BottomTab.Home,
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var homeSearchBarVisible by androidx.compose.runtime.remember {
        mutableStateOf(true)
    }
    val navAdGateway = org.koin.compose.koinInject<com.studies.rrbmustudies.ads.NavAdGateway>()
    val notificationsViewModel: NotificationsViewModel = koinViewModel()
    val hasUnread by notificationsViewModel.hasUnread.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            RrbmuTopBar(
                showLogo = selectedTab == BottomTab.Home,
                onMenuClick = null,
                onSearchClick = if (selectedTab == BottomTab.Home && !homeSearchBarVisible) {
                    onSearchClick
                } else {
                    null
                },
                onNotificationsClick = { selectedTab = BottomTab.Notifications },
                showNotificationDot = hasUnread,
            )
        },
        bottomBar = {
            RrbmuBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab != selectedTab) {
                        selectedTab = tab
                        navAdGateway.onTabSwitched(isAdmin)
                    }
                },
            )
        },
    ) { padding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                BottomTab.Home -> HomeScreen(
                    onCourseClick = onCourseClick,
                    onPaperClick = onPaperClick,
                    onSearchClick = onSearchClick,
                    onAdClick = onAdClick,
                    onSeeAllCourses = { selectedTab = BottomTab.Courses },
                    onSearchBarVisibilityChanged = { homeSearchBarVisible = it },
                )
                BottomTab.Courses -> CoursesScreen(onCourseClick = onCourseClick)
                BottomTab.Notifications -> NotificationsScreen(
                    onNotificationClick = { notification: AppNotification ->
                        onNotificationDetail(notification.id)
                    },
                    onOpenSettings = onSettings,
                    viewModel = notificationsViewModel,
                )
                BottomTab.More -> MoreScreen(
                    isAdmin = isAdmin,
                    onNavigateToFeedback = onFeedback,
                    onNavigateToAbout = onAbout,
                    onNavigateToSettings = onSettings,
                    onOpenWebView = onWebLink,
                    onAdminDashboard = onAdminDashboard,
                    onLoginClick = onSettings,
                )
            }
        }
    }
}
