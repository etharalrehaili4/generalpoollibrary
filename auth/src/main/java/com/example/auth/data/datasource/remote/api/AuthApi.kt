package com.example.auth.data.datasource.remote.api

import com.example.auth.data.datasource.model.LoginRequest
import com.example.auth.data.datasource.model.LoginResponse
import com.example.auth.data.datasource.model.RefreshTokenRequest
import com.example.auth.data.datasource.model.RefreshTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(
        @Body req: LoginRequest,
    ): LoginResponse

    @POST("refresh-token")
    suspend fun refreshToken(
        @Body req: RefreshTokenRequest,
    ): RefreshTokenResponse
}