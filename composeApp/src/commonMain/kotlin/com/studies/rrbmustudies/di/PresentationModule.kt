package com.studies.rrbmustudies.di

import com.studies.rrbmustudies.ads.NoOpPaperAdGateway
import com.studies.rrbmustudies.ads.PaperAdGateway
import com.studies.rrbmustudies.presentation.admin.AdminLoginViewModel
import com.studies.rrbmustudies.presentation.admin.AdminViewModel
import com.studies.rrbmustudies.presentation.admin.ManageCoursesViewModel
import com.studies.rrbmustudies.presentation.admin.ManageHomeAdsViewModel
import com.studies.rrbmustudies.presentation.admin.ManageNotificationsViewModel
import com.studies.rrbmustudies.presentation.admin.UploadPaperViewModel
import com.studies.rrbmustudies.presentation.courses.CourseDetailViewModel
import com.studies.rrbmustudies.presentation.courses.CoursesViewModel
import com.studies.rrbmustudies.presentation.courses.PartPapersViewModel
import com.studies.rrbmustudies.presentation.feedback.FeedbackViewModel
import com.studies.rrbmustudies.presentation.home.HomeViewModel
import com.studies.rrbmustudies.presentation.notifications.NotificationDetailViewModel
import com.studies.rrbmustudies.presentation.notifications.NotificationsViewModel
import com.studies.rrbmustudies.presentation.paper.PaperDetailViewModel
import com.studies.rrbmustudies.presentation.settings.AppSettingsViewModel
import com.studies.rrbmustudies.presentation.settings.DownloadedPapersViewModel
import com.studies.rrbmustudies.presentation.settings.SettingsViewModel
import com.studies.rrbmustudies.presentation.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    single<PaperAdGateway> { NoOpPaperAdGateway }
    viewModelOf(::AdminViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::CoursesViewModel)
    viewModelOf(::AppSettingsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::DownloadedPapersViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::NotificationsViewModel)
    viewModelOf(::FeedbackViewModel)
    viewModelOf(::AdminLoginViewModel)
    viewModelOf(::ManageHomeAdsViewModel)
    viewModelOf(::ManageCoursesViewModel)
    viewModelOf(::ManageNotificationsViewModel)

    viewModel { (courseId: String, courseName: String) ->
        CourseDetailViewModel(get(), get(), get(), courseId, courseName)
    }
    viewModel { (courseId: String, systemId: String, partId: String, partName: String, isAdmin: Boolean) ->
        PartPapersViewModel(get(), courseId, systemId, partId, partName, isAdmin)
    }
    viewModel { (courseId: String, systemId: String, partId: String, paperId: String) ->
        PaperDetailViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            courseId, systemId, partId, paperId,
        )
    }
    viewModel { (notificationId: String) ->
        NotificationDetailViewModel(notificationId, get(), get())
    }
    viewModel { (courseId: String, systemId: String, partId: String) ->
        UploadPaperViewModel(get(), get(), get(), get(), get(), courseId, systemId, partId)
    }
}
