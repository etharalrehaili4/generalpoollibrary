package com.ntg.network.core

import android.annotation.SuppressLint
import android.content.Context
import com.ntg.network.authheader.AuthInterceptor
import com.ntg.network.authheader.TokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.ntg.network.authheader.TokenRefresher
import com.ntg.network.authheader.TokenStore
import okhttp3.Interceptor

data class NetworkConfig(val baseUrl: String, val supabaseKey: String)

@SuppressLint("StaticFieldLeak")
object RetrofitFactory {
    private lateinit var appCtx: Context
    private lateinit var cfg: NetworkConfig
    lateinit var tokenStore: TokenStore
    private lateinit var tokenRefresher: TokenRefresher

    fun init(appContext: Context, config: NetworkConfig, tokenStore: TokenStore, tokenRefresher: TokenRefresher) {
        appCtx = appContext.applicationContext
        cfg = config
        this.tokenStore = tokenStore
        this.tokenRefresher = tokenRefresher
    }

    // لا-مصادقة
    private val noAuthHeaders = Interceptor { chain ->
        val key = cfg.supabaseKey
        chain.proceed(
            chain.request().newBuilder()
                .header("Authorization", "Bearer $key")
                .header("apikey", key)
                .build()
        )
    }

    private val noAuthOkHttp by lazy {
        OkHttpClient.Builder()
            .addInterceptor(noAuthHeaders)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    private val noAuthRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(cfg.baseUrl)
            .client(noAuthOkHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createNoAuth(service: Class<T>): T = noAuthRetrofit.create(service)

    // بمصادقة
    private val authedOkHttp by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore, cfg.supabaseKey))
            .authenticator(TokenAuthenticator(tokenStore, tokenRefresher))
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    private val authedRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(cfg.baseUrl)
            .client(authedOkHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createAuthed(service: Class<T>): T = authedRetrofit.create(service)
}