package com.studies.rrbmustudies.domain.repository

import com.studies.rrbmustudies.domain.model.AdminUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AdminUser?>
    val isAdmin: Flow<Boolean>
    suspend fun signIn(email: String, password: String): Result<AdminUser>
    suspend fun signOut(): Result<Unit>
}
