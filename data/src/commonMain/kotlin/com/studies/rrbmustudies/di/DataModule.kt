package com.studies.rrbmustudies.di

import com.studies.rrbmustudies.data.local.DownloadedPaperLocalDataSource
import com.studies.rrbmustudies.data.local.PaperLocalDataSource
import com.studies.rrbmustudies.data.local.SqlDelightDownloadedPaperLocalDataSource
import com.studies.rrbmustudies.data.local.SqlDelightPaperLocalDataSource
import com.studies.rrbmustudies.data.repository.PdfVaultRepositoryImpl
import com.studies.rrbmustudies.data.remote.AuthRemoteDataSource
import com.studies.rrbmustudies.data.remote.CourseRemoteDataSource
import com.studies.rrbmustudies.data.remote.FeedbackRemoteDataSource
import com.studies.rrbmustudies.data.remote.HomeAdRemoteDataSource
import com.studies.rrbmustudies.data.remote.NotificationRemoteDataSource
import com.studies.rrbmustudies.data.remote.PaperRemoteDataSource
import com.studies.rrbmustudies.data.remote.SearchRemoteDataSource
import com.studies.rrbmustudies.data.remote.StorageRemoteDataSource
import com.studies.rrbmustudies.data.remote.firestore.FirebaseStorageDataSource
import com.studies.rrbmustudies.data.remote.firestore.FirestoreAuthDataSource
import com.studies.rrbmustudies.data.remote.firestore.FirestoreCourseDataSource
import com.studies.rrbmustudies.data.remote.firestore.FirestoreFeedbackDataSource
import com.studies.rrbmustudies.data.remote.firestore.FirestoreHomeAdDataSource
import com.studies.rrbmustudies.data.remote.firestore.FirestoreNotificationDataSource
import com.studies.rrbmustudies.data.remote.firestore.FirestorePaperDataSource
import com.studies.rrbmustudies.data.remote.firestore.FirestoreSearchDataSource
import com.studies.rrbmustudies.data.repository.AuthRepositoryImpl
import com.studies.rrbmustudies.data.repository.FirebaseAnalyticsRepository
import com.studies.rrbmustudies.data.repository.SettingsRepositoryImpl
import com.studies.rrbmustudies.data.repository.CourseRepositoryImpl
import com.studies.rrbmustudies.data.repository.FeedbackRepositoryImpl
import com.studies.rrbmustudies.data.repository.HomeAdRepositoryImpl
import com.studies.rrbmustudies.data.repository.NotificationRepositoryImpl
import com.studies.rrbmustudies.data.repository.PaperRepositoryImpl
import com.studies.rrbmustudies.data.repository.SearchRepositoryImpl
import com.studies.rrbmustudies.domain.repository.AnalyticsRepository
import com.studies.rrbmustudies.domain.repository.AuthRepository
import com.studies.rrbmustudies.domain.repository.CourseRepository
import com.studies.rrbmustudies.domain.repository.FeedbackRepository
import com.studies.rrbmustudies.domain.repository.HomeAdRepository
import com.studies.rrbmustudies.domain.repository.NotificationRepository
import com.studies.rrbmustudies.domain.repository.PaperRepository
import com.studies.rrbmustudies.domain.repository.PdfVaultRepository
import com.studies.rrbmustudies.domain.repository.SearchRepository
import com.studies.rrbmustudies.domain.repository.SettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single<PaperLocalDataSource> { SqlDelightPaperLocalDataSource(get()) }
    single<DownloadedPaperLocalDataSource> { SqlDelightDownloadedPaperLocalDataSource(get()) }
    singleOf(::PdfVaultRepositoryImpl) bind PdfVaultRepository::class

    single<CourseRemoteDataSource> { FirestoreCourseDataSource() }
    single<PaperRemoteDataSource> { FirestorePaperDataSource() }
    single<SearchRemoteDataSource> { FirestoreSearchDataSource() }
    single<HomeAdRemoteDataSource> { FirestoreHomeAdDataSource() }
    single<NotificationRemoteDataSource> { FirestoreNotificationDataSource() }
    single<FeedbackRemoteDataSource> { FirestoreFeedbackDataSource() }
    single<AuthRemoteDataSource> { FirestoreAuthDataSource() }
    single<StorageRemoteDataSource> { FirebaseStorageDataSource() }

    singleOf(::CourseRepositoryImpl) bind CourseRepository::class
    singleOf(::PaperRepositoryImpl) bind PaperRepository::class
    singleOf(::SearchRepositoryImpl) bind SearchRepository::class
    singleOf(::HomeAdRepositoryImpl) bind HomeAdRepository::class
    singleOf(::NotificationRepositoryImpl) bind NotificationRepository::class
    singleOf(::FeedbackRepositoryImpl) bind FeedbackRepository::class
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    single<AnalyticsRepository> { FirebaseAnalyticsRepository() }
}
