package com.ntg.lmd.utils

import com.ntg.lmd.authentication.data.datasource.model.RefreshTokenRequest
import com.ntg.lmd.authentication.data.datasource.remote.api.AuthApi
import com.ntg.network.authheader.TokenRefresher
import com.ntg.network.authheader.Tokens

class SupabaseTokenRefresher(private val api: AuthApi) : TokenRefresher {
    override suspend fun refresh(refreshToken: String): Tokens? {
        val d = api.refreshToken(RefreshTokenRequest(refreshToken)).data ?: return null
        return Tokens(accessToken = d.accessToken, refreshToken = d.refreshToken)
    }
}