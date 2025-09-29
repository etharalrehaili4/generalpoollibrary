package com.example.auth.utils

import com.example.auth.data.datasource.model.RefreshTokenRequest
import com.example.auth.data.datasource.remote.api.AuthApi
import com.ntg.network.authheader.TokenRefresher
import com.ntg.network.authheader.Tokens


class SupabaseTokenRefresher(private val api: AuthApi) : TokenRefresher {
    override suspend fun refresh(refreshToken: String): Tokens? {
        val d = api.refreshToken(RefreshTokenRequest(refreshToken)).data ?: return null
        return Tokens(accessToken = d.accessToken, refreshToken = d.refreshToken)
    }
}