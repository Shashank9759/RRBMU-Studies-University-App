package com.studies.rrbmustudies.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.usecase.SignInAdminUseCase
import com.studies.rrbmustudies.ui.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminLoginUiState(
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

class AdminLoginViewModel(
    private val signInAdminUseCase: SignInAdminUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminLoginUiState())
    val state: StateFlow<AdminLoginUiState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(showPassword = !_state.value.showPassword)
    }

    fun reset() {
        _state.value = AdminLoginUiState()
    }

    fun signIn() {
        val current = _state.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _state.value = current.copy(error = "Email and password are required")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, error = null)
            val result = signInAdminUseCase(current.email.trim(), current.password)
            result.fold(
                onSuccess = {
                    _state.value = AdminLoginUiState(isSuccess = true)
                },
                onFailure = { e ->
                    _state.value = current.copy(
                        isLoading = false,
                        error = e.toUserMessage("Couldn't sign in. Check your email and password."),
                    )
                },
            )
        }
    }
}
