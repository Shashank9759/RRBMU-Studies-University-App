package com.studies.rrbmustudies.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.AdminUser
import com.studies.rrbmustudies.domain.usecase.ObserveAdminStateUseCase
import com.studies.rrbmustudies.domain.usecase.SignInAdminUseCase
import com.studies.rrbmustudies.domain.usecase.SignOutAdminUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    observeAdminStateUseCase: ObserveAdminStateUseCase,
    private val signInAdminUseCase: SignInAdminUseCase,
    private val signOutAdminUseCase: SignOutAdminUseCase,
) : ViewModel() {

    val isAdmin: StateFlow<Boolean> = observeAdminStateUseCase.isAdmin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val currentUser: StateFlow<AdminUser?> = observeAdminStateUseCase.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    suspend fun signIn(email: String, password: String): Result<AdminUser> =
        signInAdminUseCase(email, password)

    fun signOut() {
        viewModelScope.launch { signOutAdminUseCase() }
    }
}
