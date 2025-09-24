package com.ntg.lmd

import android.app.Application
import com.ntg.core.location.di.locationModule
import com.ntg.lmd.utils.SupabaseTokenRefresher
import com.ntg.lmd.authentication.data.datasource.remote.api.AuthApi
import com.ntg.lmd.di.MyOrderMyPoolModule
import com.ntg.lmd.di.authModule
import com.ntg.lmd.di.deliveriesLogModule
import com.ntg.lmd.di.generalPoolModule
import com.ntg.lmd.di.monitorModule
import com.ntg.lmd.di.networkModule
import com.ntg.lmd.di.ordersHistoryModule
import com.ntg.lmd.di.securityModule
import com.ntg.lmd.di.settingsModule
import com.ntg.lmd.di.socketModule
import com.ntg.network.authheader.SecureTokenStore
import com.ntg.network.authheader.TokenRefresher
import com.ntg.network.authheader.TokenStore
import com.ntg.network.core.NetworkConfig
import com.ntg.network.core.RetrofitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MyApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        val cfg = NetworkConfig(
            baseUrl = BuildConfig.BASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        )

        val tokenStore: TokenStore = SecureTokenStore(this)

        val authApiNoAuth = RetrofitFactory.apply {
            init(this@MyApp, cfg, tokenStore, object : TokenRefresher {
                override suspend fun refresh(refreshToken: String) = null
            })
        }.createNoAuth(AuthApi::class.java)

        val refresher = SupabaseTokenRefresher(authApiNoAuth)

        RetrofitFactory.init(
            appContext = this,
            config = cfg,
            tokenStore = tokenStore,
            tokenRefresher = refresher
        )
        startKoin {
            androidContext(this@MyApp)
            modules(
                listOf(
                    networkModule,
                    authModule,
                    securityModule,
                    socketModule,
                    monitorModule,
                    settingsModule,
                    MyOrderMyPoolModule,
                    ordersHistoryModule,
                    deliveriesLogModule,
                    generalPoolModule,
                ),
            )
        }
    }
    }

