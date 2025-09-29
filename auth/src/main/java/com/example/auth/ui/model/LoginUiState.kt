package com.example.auth.ui.model

import com.example.auth.ui.viewmodel.ValidationError
import com.example.auth.utils.NetworkResult
import com.example.auth.R

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    @androidx.annotation.StringRes val message: Int? = null,
    val errorMessage: String? = null,
    val isFormValid: Boolean = false,
    val usernameError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val showUsernameError: Boolean = false,
    val showPasswordError: Boolean = false,
    val loginSuccess: Boolean = false,
    val displayName: String? = null,
)

sealed class UiText {
    data class DynamicString(
        val value: String,
    ) : UiText()

    data class StringResource(
        val resId: Int,
    ) : UiText()
}

fun LoginUiState.afterLoginResult(result: NetworkResult<Unit>): LoginUiState =
    when (result) {
        is NetworkResult.Success ->
            copy(
                isLoading = false,
                loginSuccess = true,
                message = R.string.msg_welcome,
            )
        is NetworkResult.Error ->
            copy(
                isLoading = false,
                loginSuccess = false,
                message = R.string.error_invalid_credentials,
            )
        is NetworkResult.Loading ->
            copy(
                // shouldn't happen here, but keep safe
                isLoading = true,
            )
    }
