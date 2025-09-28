package com.ntg.lmd.mainscreen.ui.viewmodel

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.example.generalpool.vm.GeneralPoolViewModel as LibVM
import com.example.generalpool.repository.LiveOrdersRepository as LibLiveRepo
import com.example.generalpool.repository.UpdateOrdersStatusRepository as LibUpdateRepo
import com.example.generalpool.models.OrderInfo
import com.ntg.lmd.mainscreen.domain.model.OrderStatus

val generalPoolLibModule = module {
    single<LibLiveRepo> { LiveOrdersRepositoryAdapter(appRepo = get()) }
    single<DeviceLocationProvider> { DeviceLocationProviderAdapter(getDeviceLocations = get()) }

    single<LibUpdateRepo> {
        UpdateOrdersStatusRepositoryAdapter { orderId, statusId, assigned ->
            val updater: UpdateOrderStatusViewModel = get()
            val target = OrderStatus.fromId(statusId) ?: OrderStatus.ADDED
            updater.update(orderId = orderId, targetStatus = target, assignedAgentId = assigned)

            OrderInfo(
                id = orderId,
                name = "",
                orderNumber = orderId,
                distanceKm = Double.POSITIVE_INFINITY,
                lat = 0.0,
                lng = 0.0,
                statusId = statusId,
                assignedAgentId = assigned,
                details = null,
                customerPhone = null
            )
        }
    }

    viewModel {
        LibVM(
            liveRepo = get(),
            updateRepo = get(),
            deviceLocation = get(),
        )
    }
}
