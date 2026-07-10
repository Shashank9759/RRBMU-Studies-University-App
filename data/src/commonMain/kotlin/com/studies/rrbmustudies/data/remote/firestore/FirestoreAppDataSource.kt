package com.studies.rrbmustudies.data.remote.firestore

import com.studies.rrbmustudies.data.dto.AdminDto
import com.studies.rrbmustudies.data.dto.FeedbackDto
import com.studies.rrbmustudies.data.dto.HomeAdDto
import com.studies.rrbmustudies.data.dto.HomeCarouselSettingsDto
import com.studies.rrbmustudies.data.dto.NotificationDto
import com.studies.rrbmustudies.data.remote.AuthRemoteDataSource
import com.studies.rrbmustudies.data.remote.FeedbackRemoteDataSource
import com.studies.rrbmustudies.data.remote.FirestorePaths
import com.studies.rrbmustudies.data.remote.HomeAdRemoteDataSource
import com.studies.rrbmustudies.data.remote.NotificationRemoteDataSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreHomeAdDataSource(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : HomeAdRemoteDataSource {

    override fun observeActiveAds(): Flow<List<Pair<String, HomeAdDto>>> =
        firestore.collection(FirestorePaths.HOME_ADS)
            .where { "isActive" equalTo true }
            .orderBy("order", Direction.ASCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.data<HomeAdDto>()?.let { doc.id to it }
                }
            }

    override fun observeAllAds(): Flow<List<Pair<String, HomeAdDto>>> =
        firestore.collection(FirestorePaths.HOME_ADS)
            .orderBy("order", Direction.ASCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.data<HomeAdDto>()?.let { doc.id to it }
                }
            }

    override fun observeCarouselSettings(): Flow<HomeCarouselSettingsDto> =
        firestore.collection(FirestorePaths.APP_SETTINGS)
            .document(FirestorePaths.HOME_CAROUSEL_SETTINGS)
            .snapshots
            .map { snapshot ->
                snapshot.data<HomeCarouselSettingsDto>() ?: HomeCarouselSettingsDto()
            }

    override suspend fun saveCarouselSettings(settings: HomeCarouselSettingsDto) {
        firestore.collection(FirestorePaths.APP_SETTINGS)
            .document(FirestorePaths.HOME_CAROUSEL_SETTINGS)
            .set(settings, merge = true)
    }

    override suspend fun saveAd(id: String, ad: HomeAdDto) {
        firestore.collection(FirestorePaths.HOME_ADS).document(id).set(ad, merge = true)
    }

    override suspend fun deleteAd(id: String) {
        firestore.collection(FirestorePaths.HOME_ADS).document(id).delete()
    }
}

class FirestoreNotificationDataSource(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : NotificationRemoteDataSource {

    override fun observeNotifications(): Flow<List<Pair<String, NotificationDto>>> =
        firestore.collection(FirestorePaths.NOTIFICATIONS)
            .where { "isActive" equalTo true }
            .orderBy("createdAt", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.data<NotificationDto>()?.let { doc.id to it }
                }
            }

    override fun observeAllNotifications(): Flow<List<Pair<String, NotificationDto>>> =
        firestore.collection(FirestorePaths.NOTIFICATIONS)
            .orderBy("createdAt", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.data<NotificationDto>()?.let { doc.id to it }
                }
            }

    override suspend fun saveNotification(id: String, notification: NotificationDto) {
        firestore.collection(FirestorePaths.NOTIFICATIONS).document(id).set(notification, merge = true)
    }

    override suspend fun deleteNotification(id: String) {
        firestore.collection(FirestorePaths.NOTIFICATIONS).document(id).delete()
    }
}

class FirestoreFeedbackDataSource(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : FeedbackRemoteDataSource {

    override suspend fun submitFeedback(id: String, feedback: FeedbackDto) {
        firestore.collection(FirestorePaths.FEEDBACK).document(id).set(feedback)
    }
}

class FirestoreAuthDataSource(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : AuthRemoteDataSource {

    override val currentUid: Flow<String?> =
        auth.authStateChanged.map { it?.uid }

    override suspend fun signIn(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password)
        return result.user?.uid ?: error("Sign in failed: no user returned")
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun isAdmin(uid: String): Boolean {
        val doc = firestore.collection(FirestorePaths.ADMINS).document(uid).get()
        return doc.exists
    }

    override suspend fun getAdminProfile(uid: String): AdminDto? {
        val doc = firestore.collection(FirestorePaths.ADMINS).document(uid).get()
        if (!doc.exists) return null
        return runCatching { doc.data<AdminDto>() }.getOrNull() ?: AdminDto()
    }
}
