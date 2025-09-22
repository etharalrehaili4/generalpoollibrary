package com.ntg.network.authheader

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val store: TokenStore,
    private val supabaseKey: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val path = req.url.encodedPath
        val isAuth = path.endsWith("/login") || path.endsWith("/refresh-token")

        val b = req.newBuilder().header("apikey", supabaseKey)
        if (isAuth) {
            b.header("Authorization", "Bearer $supabaseKey")
        } else {
            store.getAccessToken()?.takeIf { it.isNotBlank() }?.let {
                b.header("Authorization", "Bearer $it")
            }
        }
        return chain.proceed(b.build())
    }
}
