package com.studies.rrbmustudies.di

import com.studies.rrbmustudies.domain.usecase.CacheViewedPaperUseCase
import com.studies.rrbmustudies.domain.usecase.ClearPdfVaultUseCase
import com.studies.rrbmustudies.domain.usecase.CreateCourseUseCase
import com.studies.rrbmustudies.domain.usecase.CreateHomeAdUseCase
import com.studies.rrbmustudies.domain.usecase.CreateNotificationUseCase
import com.studies.rrbmustudies.domain.usecase.DeleteHomeAdUseCase
import com.studies.rrbmustudies.domain.usecase.DeleteNotificationUseCase
import com.studies.rrbmustudies.domain.usecase.DeletePaperUseCase
import com.studies.rrbmustudies.domain.usecase.DownloadPaperToVaultUseCase
import com.studies.rrbmustudies.domain.usecase.GetAllCoursesUseCase
import com.studies.rrbmustudies.domain.usecase.GetAllNotificationsUseCase
import com.studies.rrbmustudies.domain.usecase.GetCachedPapersUseCase
import com.studies.rrbmustudies.domain.usecase.GetCourseSystemsUseCase
import com.studies.rrbmustudies.domain.usecase.GetCourseUseCase
import com.studies.rrbmustudies.domain.usecase.GetCoursesUseCase
import com.studies.rrbmustudies.domain.usecase.GetHomeAdsUseCase
import com.studies.rrbmustudies.domain.usecase.GetNotificationsUseCase
import com.studies.rrbmustudies.domain.usecase.GetPaperUseCase
import com.studies.rrbmustudies.domain.usecase.GetPapersUseCase
import com.studies.rrbmustudies.domain.usecase.GetPartsUseCase
import com.studies.rrbmustudies.domain.usecase.GetRecentPapersUseCase
import com.studies.rrbmustudies.domain.usecase.GetVaultDownloadUseCase
import com.studies.rrbmustudies.domain.usecase.IncrementDownloadCountUseCase
import com.studies.rrbmustudies.domain.usecase.LogDownloadUseCase
import com.studies.rrbmustudies.domain.usecase.LogPaperViewUseCase
import com.studies.rrbmustudies.domain.usecase.LogSearchUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveAdminStateUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveDownloadedPapersUseCase
import com.studies.rrbmustudies.domain.usecase.ObservePaperDownloadedUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveThemeModeUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveVaultStorageUseCase
import com.studies.rrbmustudies.domain.usecase.SearchPapersUseCase
import com.studies.rrbmustudies.domain.usecase.SetThemeModeUseCase
import com.studies.rrbmustudies.domain.usecase.SignInAdminUseCase
import com.studies.rrbmustudies.domain.usecase.SignOutAdminUseCase
import com.studies.rrbmustudies.domain.usecase.SubmitFeedbackUseCase
import com.studies.rrbmustudies.domain.usecase.UpdateCourseUseCase
import com.studies.rrbmustudies.domain.usecase.UpdateHomeAdUseCase
import com.studies.rrbmustudies.domain.usecase.UpdateNotificationUseCase
import com.studies.rrbmustudies.domain.usecase.UpdatePaperUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveHomeCarouselSettingsUseCase
import com.studies.rrbmustudies.domain.usecase.ReorderHomeAdsUseCase
import com.studies.rrbmustudies.domain.usecase.SaveHomeCarouselSettingsUseCase
import com.studies.rrbmustudies.domain.usecase.UploadCourseBackgroundUseCase
import com.studies.rrbmustudies.domain.usecase.UploadHomeAdImageUseCase
import com.studies.rrbmustudies.domain.usecase.UploadNotificationAttachmentUseCase
import com.studies.rrbmustudies.domain.usecase.UploadPaperUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetCoursesUseCase)
    factoryOf(::GetAllCoursesUseCase)
    factoryOf(::GetCourseUseCase)
    factoryOf(::CreateCourseUseCase)
    factoryOf(::UpdateCourseUseCase)
    factoryOf(::GetCourseSystemsUseCase)
    factoryOf(::GetPartsUseCase)
    factoryOf(::GetPapersUseCase)
    factoryOf(::GetPaperUseCase)
    factoryOf(::GetRecentPapersUseCase)
    factoryOf(::GetCachedPapersUseCase)
    factoryOf(::CacheViewedPaperUseCase)
    factoryOf(::GetHomeAdsUseCase)
    factoryOf(::SearchPapersUseCase)
    factoryOf(::IncrementDownloadCountUseCase)
    factoryOf(::GetNotificationsUseCase)
    factoryOf(::SubmitFeedbackUseCase)
    factoryOf(::ObserveThemeModeUseCase)
    factoryOf(::SetThemeModeUseCase)
    factoryOf(::LogPaperViewUseCase)
    factoryOf(::LogDownloadUseCase)
    factoryOf(::LogSearchUseCase)

    factoryOf(::ObserveVaultStorageUseCase)
    factoryOf(::ObserveDownloadedPapersUseCase)
    factoryOf(::ObservePaperDownloadedUseCase)
    factoryOf(::DownloadPaperToVaultUseCase)
    factoryOf(::GetVaultDownloadUseCase)
    factoryOf(::ClearPdfVaultUseCase)

    factoryOf(::ObserveAdminStateUseCase)
    factoryOf(::SignInAdminUseCase)
    factoryOf(::SignOutAdminUseCase)
    factoryOf(::CreateHomeAdUseCase)
    factoryOf(::UpdateHomeAdUseCase)
    factoryOf(::DeleteHomeAdUseCase)
    factoryOf(::UploadHomeAdImageUseCase)
    factoryOf(::ObserveHomeCarouselSettingsUseCase)
    factoryOf(::SaveHomeCarouselSettingsUseCase)
    factoryOf(::ReorderHomeAdsUseCase)
    factoryOf(::UploadCourseBackgroundUseCase)
    factoryOf(::GetAllNotificationsUseCase)
    factoryOf(::CreateNotificationUseCase)
    factoryOf(::UpdateNotificationUseCase)
    factoryOf(::DeleteNotificationUseCase)
    factoryOf(::UploadNotificationAttachmentUseCase)
    factoryOf(::UploadPaperUseCase)
    factoryOf(::UpdatePaperUseCase)
    factoryOf(::DeletePaperUseCase)
}
