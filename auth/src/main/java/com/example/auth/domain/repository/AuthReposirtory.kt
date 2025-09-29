package com.example.auth.domain.repository

import com.example.auth.data.datasource.model.User
import com.example.auth.utils.NetworkResult


interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): NetworkResult<Unit>

    fun getCurrentUser(): User?

    fun isAuthenticated(): Boolean

    val lastLoginName: String?
}
