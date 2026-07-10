package com.studies.rrbmustudies.data.repository

import com.studies.rrbmustudies.data.mapper.toDomain
import com.studies.rrbmustudies.data.remote.AuthRemoteDataSource
import com.studies.rrbmustudies.domain.model.AdminUser
import com.studies.rrbmustudies.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
) : AuthRepository {

    override val currentUser: Flow<AdminUser?> =
        remote.currentUid.flatMapLatest { uid ->
            if (uid == null) {
                flowOf<AdminUser?>(null)
            } else {
                flow {
                    val profile = remote.getAdminProfile(uid)
                    emit(profile?.toDomain(uid) ?: AdminUser(uid = uid, email = ""))
                }
            }
        }.distinctUntilChanged()

    override val isAdmin: Flow<Boolean> =
        remote.currentUid.flatMapLatest { uid ->
            flow {
                emit(uid != null && remote.isAdmin(uid))
            }
        }.distinctUntilChanged()

    override suspend fun signIn(email: String, password: String): Result<AdminUser> =
        runCatching {
            val uid = remote.signIn(email, password)
            val isAdmin = remote.isAdmin(uid)
            if (!isAdmin) {
                remote.signOut()
                error("Not authorized as administrator")
            }
            remote.getAdminProfile(uid)?.toDomain(uid)
                ?: AdminUser(uid = uid, email = email)
        }

    override suspend fun signOut(): Result<Unit> = runCatching {
        remote.signOut()
    }
}
