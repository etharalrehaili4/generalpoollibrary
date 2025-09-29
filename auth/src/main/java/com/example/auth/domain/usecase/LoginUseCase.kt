package com.example.auth.domain.usecase

import com.example.auth.data.datasource.model.User
import com.example.auth.domain.repository.AuthRepository
import com.example.auth.utils.NetworkError
import com.example.auth.utils.NetworkResult


class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend fun execute(
        email: String,
        password: String,
    ): NetworkResult<User> =
        when (val result = authRepository.login(email, password)) {
            is NetworkResult.Success<*> -> {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    NetworkResult.Success(user)
                } else {
                    NetworkResult.Error(
                        NetworkError.BadRequest("Failed to retrieve user after login"),
                    )
                }
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }

    fun getLastLoginName(): String? = authRepository.lastLoginName
}
