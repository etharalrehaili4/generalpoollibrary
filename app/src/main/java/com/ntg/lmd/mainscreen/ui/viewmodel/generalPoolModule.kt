package com.ntg.lmd.mainscreen.ui.viewmodel

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.example.generalpool.GeneralPoolViewModel as LibGPViewModel
import com.ntg.lmd.mainscreen.domain.repository.LiveOrdersRepository as AppLiveRepo
import com.ntg.core.location.location.domain.usecase.GetDeviceLocationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

val generalPoolLibModule = module {

    // A shared IO scope for adapters that need stateIn
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    // Adapters
    single {
        LiveOrdersRepositoryAdapter(
            appRepo = get<AppLiveRepo>(),
            scope = get()
        )
    }

    single { DeviceLocationProviderAdapter(get<GetDeviceLocationsUseCase>()) }

    // You must supply the update lambda:
    /**
     * Example: call your existing update flow (replace with your real repo/usecase).
     * Suppose you have: UpdateOrderStatusUseCase(orderId, statusId, assignedAgentId) -> OrderInfo (library’s)
     */
    single {
        UpdateOrdersStatusRepositoryAdapter { orderId, statusId, assignedAgentId ->
            // TODO: Replace with your real implementation:
            // e.g. get<UpdateOrderStatusUseCase>()(orderId, statusId, assignedAgentId)
            // Must return com.example.generalpool.models.OrderInfo
            error("Plug your update implementation here and return a Lib OrderInfo")
        }
    }

    // Library ViewModel
    viewModel {
        LibGPViewModel(
            liveRepo = get(),
            updateRepo = get(),
            deviceLocation = get()
        )
    }
}
