package com.ntg.lmd.di

import com.example.auth.data.datasource.remote.api.AuthApi
import com.example.auth.data.repositoryImp.AuthRepositoryImp
import com.example.auth.domain.repository.AuthRepository
import com.example.auth.domain.usecase.LoginUseCase
import com.example.auth.settings.data.SettingsPreferenceDataSource
import com.example.auth.settings.ui.viewmodel.SettingsViewModel
import com.example.auth.ui.viewmodel.LoginViewModel
import com.example.auth.utils.LogoutManager
import com.example.auth.utils.SecureUserStore
import com.example.generalpool.data.datasource.remote.LiveOrdersApiService
import com.example.generalpool.data.datasource.remote.UpdatetOrdersStatusApi
import com.example.generalpool.data.repository.LiveOrdersRepositoryImpl
import com.example.generalpool.data.repository.OrdersSocketBridge
import com.example.generalpool.data.repository.UpdateOrdersStatusRepositoryImpl
import com.example.generalpool.domain.repository.LiveOrdersRepository
import com.example.generalpool.domain.repository.UpdateOrdersStatusRepository
import com.example.generalpool.domain.usecase.LoadOrdersUseCase
import com.example.generalpool.domain.usecase.OrdersRealtimeUseCase
import com.example.generalpool.domain.usecase.UpdateOrderStatusUseCase
import com.example.generalpool.ui.vm.GeneralPoolViewModel
import com.example.generalpool.ui.vm.UpdateOrderStatusViewModel
import com.example.myorderhistoryanddelivery.delivery.data.remote.api.DeliveriesLogApi
import com.example.myorderhistoryanddelivery.delivery.data.remote.repositoryimpl.DeliveriesLogRepositoryImpl
import com.example.myorderhistoryanddelivery.delivery.domain.repository.DeliveriesLogRepository
import com.example.myorderhistoryanddelivery.delivery.domain.usecase.GetDeliveriesLogFromApiUseCase
import com.example.myorderhistoryanddelivery.delivery.ui.viewmodel.DeliveriesLogViewModel
import com.example.myorderhistoryanddelivery.order.data.remote.api.OrdersHistoryApi
import com.example.myorderhistoryanddelivery.order.data.remote.repositoryimpl.OrdersRepositoryImpl
import com.example.myorderhistoryanddelivery.order.domain.repository.OrdersRepository
import com.example.myorderhistoryanddelivery.order.domain.usecase.GetOrdersUseCase
import com.example.myorderhistoryanddelivery.order.ui.viewmodel.OrderHistoryViewModel
import com.ntg.core.location.location.domain.repository.LocationRepository
import com.ntg.core.location.location.domain.usecase.ComputeDistancesUseCase
import com.ntg.core.location.location.domain.usecase.GetDeviceLocationsUseCase
import com.google.gson.Gson
import com.ntg.lmd.BuildConfig
import com.ntg.lmd.mainscreen.data.datasource.remote.GetUsersApi
import com.ntg.lmd.mainscreen.data.datasource.remote.OrdersApi
import com.ntg.lmd.mainscreen.data.repository.MyOrdersRepositoryImpl
import com.ntg.lmd.mainscreen.data.repository.OrderStore
import com.ntg.lmd.mainscreen.data.repository.OrdersChangeHandler
import com.ntg.lmd.mainscreen.data.repository.UsersRepositoryImpl
import com.ntg.lmd.mainscreen.domain.repository.MyOrdersRepository
import com.ntg.lmd.mainscreen.domain.repository.UsersRepository
import com.ntg.lmd.mainscreen.domain.usecase.GetActiveUsersUseCase
import com.ntg.lmd.mainscreen.domain.usecase.GetMyOrdersUseCase
import com.ntg.lmd.mainscreen.ui.viewmodel.ActiveAgentsViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.MyOrdersViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.MyPoolViewModel
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
    single<DeliveriesLogApi> { RetrofitFactory.createAuthed(DeliveriesLogApi::class.java) }
    single<DeliveriesLogRepository> { DeliveriesLogRepositoryImpl(get()) }
    factory { GetDeliveriesLogFromApiUseCase(get()) }
    viewModel { DeliveriesLogViewModel(get()) }
}

val generalPoolModule = module {

    // Use cases & deps
    factory { GetDeviceLocationsUseCase(get<LocationRepository>()) }
    factory { ComputeDistancesUseCase() }

    // Repository (requires API and the socket bridge)
    single<LiveOrdersRepository> { LiveOrdersRepositoryImpl(get(), get<OrdersSocketBridge>()) }

    // Use cases using the repo
    factory { LoadOrdersUseCase(get<LiveOrdersRepository>()) }
    factory { OrdersRealtimeUseCase(get<LiveOrdersRepository>()) }

    // ViewModel
    viewModel {
        GeneralPoolViewModel(
            ordersRealtime = get(),
            computeDistances = get(),
            getDeviceLocations = get(),
            loadOrdersUseCase = get(),
        )
    }

    // API
    single<LiveOrdersApiService> { RetrofitFactory.createAuthed(LiveOrdersApiService::class.java) }
}

val settingsModule = module {
    single { SettingsPreferenceDataSource(get()) }

    single {
        LogoutManager(
            tokenStore = get<SecureTokenStore>(),
            userStore = get<SecureUserStore>(),
            socket = get<SocketIntegration>()
        )
    }

    viewModel { SettingsViewModel(prefs = get(), logoutManager = get()) }
}
