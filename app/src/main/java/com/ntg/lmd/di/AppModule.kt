package com.ntg.lmd.di

import com.ntg.core.location.location.domain.repository.LocationRepository
import com.ntg.core.location.location.domain.usecase.ComputeDistancesUseCase
import com.ntg.core.location.location.domain.usecase.GetDeviceLocationsUseCase
import com.google.gson.Gson
import com.ntg.lmd.BuildConfig
import com.ntg.lmd.authentication.data.datasource.remote.api.AuthApi
import com.ntg.lmd.authentication.data.repositoryImp.AuthRepositoryImp
import com.ntg.lmd.authentication.domain.repository.AuthRepository
import com.ntg.lmd.authentication.domain.usecase.LoginUseCase
import com.ntg.lmd.authentication.ui.viewmodel.login.LoginViewModel
import com.ntg.lmd.mainscreen.data.datasource.remote.GetUsersApi
import com.ntg.lmd.mainscreen.data.datasource.remote.LiveOrdersApiService
import com.ntg.lmd.mainscreen.data.datasource.remote.OrdersApi
import com.ntg.lmd.mainscreen.data.datasource.remote.UpdatetOrdersStatusApi
import com.ntg.lmd.mainscreen.data.repository.DeliveriesLogRepositoryImpl
import com.ntg.lmd.mainscreen.data.repository.LiveOrdersRepositoryImpl
import com.ntg.lmd.mainscreen.data.repository.MyOrdersRepositoryImpl
import com.ntg.lmd.mainscreen.data.repository.OrderStore
import com.ntg.lmd.mainscreen.data.repository.OrdersChangeHandler
import com.ntg.lmd.mainscreen.data.repository.OrdersSocketBridge
import com.ntg.lmd.mainscreen.data.repository.UpdateOrdersStatusRepositoryImpl
import com.ntg.lmd.mainscreen.data.repository.UsersRepositoryImpl
import com.ntg.lmd.mainscreen.domain.repository.DeliveriesLogRepository
import com.ntg.lmd.mainscreen.domain.repository.LiveOrdersRepository
import com.ntg.lmd.mainscreen.domain.repository.MyOrdersRepository
import com.ntg.lmd.mainscreen.domain.repository.UpdateOrdersStatusRepository
import com.ntg.lmd.mainscreen.domain.repository.UsersRepository
import com.ntg.lmd.mainscreen.domain.usecase.GetActiveUsersUseCase
import com.ntg.lmd.mainscreen.domain.usecase.GetDeliveriesLogFromApiUseCase
import com.ntg.lmd.mainscreen.domain.usecase.GetMyOrdersUseCase
import com.ntg.lmd.mainscreen.domain.usecase.LoadOrdersUseCase
import com.ntg.lmd.mainscreen.domain.usecase.OrdersRealtimeUseCase
import com.ntg.lmd.mainscreen.domain.usecase.UpdateOrderStatusUseCase
import com.ntg.lmd.mainscreen.ui.viewmodel.ActiveAgentsViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.DeliveriesLogViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.GeneralPoolViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.MyOrdersViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.MyPoolViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.UpdateOrderStatusViewModel
import com.ntg.lmd.order.data.remote.OrdersHistoryApi
import com.ntg.lmd.order.data.remote.repository.OrdersRepositoryImpl
import com.ntg.lmd.order.domain.model.repository.OrdersRepository
import com.ntg.lmd.order.domain.model.usecase.GetOrdersUseCase
import com.ntg.lmd.order.ui.viewmodel.OrderHistoryViewModel
import com.ntg.lmd.settings.data.SettingsPreferenceDataSource
import com.ntg.lmd.settings.ui.viewmodel.SettingsViewModel
import com.ntg.lmd.utils.LogoutManager
import com.ntg.lmd.utils.SecureUserStore
import com.ntg.network.authheader.SecureTokenStore
import com.ntg.network.authheader.TokenStore
import com.ntg.network.connectivity.NetworkMonitor
import com.ntg.network.core.RetrofitFactory
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.ntg.network.sockets.SocketIntegration
import com.ntg.network.sockets.ChangeHandler

val authModule = module {
    // SecureUserStore
    single { SecureUserStore(get()) }

    single<AuthApi> { RetrofitFactory.createNoAuth(AuthApi::class.java) }

    // Repository
    single<AuthRepository> {
        AuthRepositoryImp(
            loginApi = get(),    // AuthApi
            store = get(),       // SecureTokenStore
            userStore = get()
        )
    }

    // UseCase + VM
    factory { LoginUseCase(get()) }
    viewModel { LoginViewModel(loginUseCase = get()) }
}


val networkModule = module {
    factory<OrdersApi> { RetrofitFactory.createAuthed(OrdersApi::class.java) }
    factory<UpdatetOrdersStatusApi> { RetrofitFactory.createAuthed(UpdatetOrdersStatusApi::class.java) }
    factory<GetUsersApi> { RetrofitFactory.createAuthed(GetUsersApi::class.java) }
}


val socketModule = module {
    single { Gson() }
    single<TokenStore> { SecureTokenStore(androidContext()) }
    single { OrderStore() }
    single<ChangeHandler> { OrdersChangeHandler(gson = get(), store = get()) }

    single {
        SocketIntegration(
            baseWsUrl = BuildConfig.WS_BASE_URL,
            tokenStore = get<TokenStore>(),
            handler = get<ChangeHandler>(),
            enableLogging = true
        ).apply {
            (get<TokenStore>() as? SecureTokenStore)?.onTokensChanged = { access, _ ->
                reconnectIfTokenChanged(access)
            }
        }
    }

    single { OrdersSocketBridge(socket = get(), store = get(), gson = get()) }
}


val monitorModule =
    module {
        single { NetworkMonitor(get()) }
    }

val securityModule = module {
    single { SecureTokenStore(androidContext()) }
    single<TokenStore> { get<SecureTokenStore>() }
}
val MyOrderMyPoolModule =
    module {
        // Repos
        single<MyOrdersRepository> { MyOrdersRepositoryImpl(get()) }
        single<UpdateOrdersStatusRepository> { UpdateOrdersStatusRepositoryImpl(get()) }
        single<UsersRepository> { UsersRepositoryImpl(get()) }

        // UseCases
        factory { GetMyOrdersUseCase(get()) }
        factory { UpdateOrderStatusUseCase(get()) }
        factory { GetActiveUsersUseCase(get()) }
        factory { ComputeDistancesUseCase() }

        // ViewModels
        viewModel {
            MyOrdersViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel<MyPoolViewModel> {
            MyPoolViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            UpdateOrderStatusViewModel(
                get(),
                get(),
            )
        }
        viewModel { ActiveAgentsViewModel(get()) }
    }

val ordersHistoryModule = module {
    single<OrdersHistoryApi> { RetrofitFactory.createAuthed(OrdersHistoryApi::class.java) }
    single<OrdersRepository> { OrdersRepositoryImpl(get()) }
    factory { GetOrdersUseCase(get()) }
    viewModel { OrderHistoryViewModel(get()) }
}

val deliveriesLogModule = module {
    single<OrdersApi> { RetrofitFactory.createAuthed(OrdersApi::class.java) }
    single<DeliveriesLogRepository> { DeliveriesLogRepositoryImpl(get()) }
    factory { GetDeliveriesLogFromApiUseCase(get()) }
    viewModel { DeliveriesLogViewModel(get()) }
}

val generalPoolModule = module {
    single<LocationRepository> { LocationRepositoryImpl(get()) }
    single<LiveOrdersRepository> { LiveOrdersRepositoryImpl(get(), get<OrdersSocketBridge>()) }

    factory { LoadOrdersUseCase(get<LiveOrdersRepository>()) }
    factory { OrdersRealtimeUseCase(get<LiveOrdersRepository>()) }
    factory { ComputeDistancesUseCase() }
    factory { GetDeviceLocationsUseCase(get<LocationRepository>()) }

    viewModel {
        GeneralPoolViewModel(
            ordersRealtime = get(),
            computeDistances = get(),
            getDeviceLocations = get(),
            loadOrdersUseCase = get(),
        )
    }

    single<LiveOrdersApiService> { RetrofitFactory.createAuthed(LiveOrdersApiService::class.java) }
}

val settingsModule = module {
    single { SettingsPreferenceDataSource(get()) }

    single {
        LogoutManager(
            tokenStore = get<SecureTokenStore>(),
            userStore  = get<SecureUserStore>(),
            socket     = get<SocketIntegration>()
        )
    }

    viewModel { SettingsViewModel(prefs = get(), logoutManager = get()) }
}
