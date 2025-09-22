package com.ntg.network.authheader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route


private const val MAX_RETRY_ATTEMPTS = 1

data class AuthConfig(
    val headerName: String = "Authorization",
    val scheme: String = "Bearer",
    val unauthorizedCodes: Set<Int> = setOf(401),
    val maxRetryAttempts: Int = MAX_RETRY_ATTEMPTS,
) {
    fun headerValue(token: String?) = if (scheme.isBlank()) token else "$scheme $token"
    fun extractTokenFrom(request: Request) =
        request.header(headerName)?.removePrefix("$scheme ")?.trim()
}

class TokenAuthenticator(
    private val store: TokenStore,
    private val refresher: TokenRefresher,
    private val config: AuthConfig = AuthConfig(),
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val had = response.request.header(config.headerName) != null
        val should = had && response.code in config.unauthorizedCodes && count(response) <= config.maxRetryAttempts
        if (!should) return null

        val failedAccess = config.extractTokenFrom(response.request)
        val tokens = runBlocking(Dispatchers.IO) {
            mutex.withLock {
                val current = store.getAccessToken()
                if (!current.isNullOrBlank() && current != failedAccess) {
                    return@withLock Tokens(current, store.getRefreshToken(), store.getAccessExpiryIso(), store.getRefreshExpiryIso())
                }
                val rt = store.getRefreshToken() ?: return@withLock null
                val newTokens = refresher.refresh(rt) ?: run { store.clear(); return@withLock null }
                store.saveTokens(newTokens)
                newTokens
            }
        } ?: return null

        return config.headerValue(tokens.accessToken)?.let {
            response.request.newBuilder()
                .header(config.headerName, it)
        }
            ?.build()
    }

    private fun count(r: Response): Int {
        var x: Response? = r; var c = 0
        while (x != null) { c++; x = x.priorResponse }
        return c
    }
}

