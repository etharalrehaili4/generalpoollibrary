package com.ntg.network.authheader

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// توكنات عامة
data class Tokens(
    val accessToken: String?,
    val refreshToken: String?,
    val accessExpiresAt: String? = null,
    val refreshExpiresAt: String? = null,
)

interface TokenStore {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(tokens: Tokens)
    fun clear()

    fun getAccessExpiryIso(): String? = null
    fun getRefreshExpiryIso(): String? = null
}

interface TokenRefresher {
    suspend fun refresh(refreshToken: String): Tokens?
}

class SecureTokenStore(ctx: Context) : TokenStore {

    private val appCtx = ctx.applicationContext

    private val masterKey = MasterKey.Builder(appCtx)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sp = EncryptedSharedPreferences.create(
        appCtx,
        "secure_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    // --- TokenStore impl ---
    override fun getAccessToken(): String? = sp.getString("access", null)
    override fun getRefreshToken(): String? = sp.getString("refresh", null)
    override fun getAccessExpiryIso(): String? = sp.getString("access_exp", null)
    override fun getRefreshExpiryIso(): String? = sp.getString("refresh_exp", null)

    override fun saveTokens(tokens: Tokens) {
        saveFromPayload(
            access = tokens.accessToken,
            refresh = tokens.refreshToken,
            expiresAt = tokens.accessExpiresAt,
            refreshExpiresAt = tokens.refreshExpiresAt
        )
    }

    override fun clear() {
        sp.edit().clear().apply()
        onTokensChanged?.invoke(null, null)
    }

    fun saveFromPayload(
        access: String?,
        refresh: String?,
        expiresAt: String?,
        refreshExpiresAt: String?,
    ) {
        val newRefresh = refresh ?: getRefreshToken()
        sp.edit()
            .putString("access", access)
            .putString("refresh", newRefresh)
            .putString("access_exp", expiresAt)
            .putString("refresh_exp", refreshExpiresAt ?: getRefreshExpiryIso())
            .apply()
        onTokensChanged?.invoke(access, newRefresh)
    }

    @Volatile
    var onTokensChanged: ((access: String?, refresh: String?) -> Unit)? = null
}
