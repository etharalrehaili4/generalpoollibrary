package com.ntg.lmd.mainscreen.ui.viewmodel

import android.location.Location
import com.example.generalpool.models.Order as LibOrder
import com.example.generalpool.models.OrderInfo as LibOrderInfo
import com.example.generalpool.repository.LiveOrdersRepository as LibLiveRepo
import com.example.generalpool.repository.UpdateOrdersStatusRepository as LibUpdateRepo
import com.ntg.core.location.location.domain.usecase.GetDeviceLocationsUseCase
import com.ntg.lmd.mainscreen.data.model.Order as AppOrder
import com.ntg.lmd.mainscreen.domain.repository.LiveOrdersRepository as AppLiveRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.generalpool.models.Coordinates as LibCoords
import com.ntg.core.location.location.domain.model.Coordinates as AppCoords
import com.ntg.core.location.location.domain.model.MapMarker


// ---------- MAPPERS ----------

private fun AppOrder.toLibOrder(): LibOrder =
    LibOrder(
        id = id,
        orderId = orderId,
        orderNumber = orderNumber,
        customerName = customerName,
        address = address,
        statusId = statusId,
        assignedAgentId = assignedAgentId,
        price = price,
        phone = phone,
        lastUpdated = lastUpdated ?: orderDate ?: deliveryTime,
        latitude = coordinates?.latitude ?: latitude,
        longitude = coordinates?.longitude ?: longitude
    )

class LiveOrdersRepositoryAdapter(
    private val appRepo: AppLiveRepo,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : LibLiveRepo {

    private val state: StateFlow<List<LibOrder>> by lazy {
        appRepo
            .orders()
            .map { list -> list.map { it.toLibOrder() } }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
    }

    override suspend fun getAllLiveOrders(pageSize: Int): Result<List<LibOrder>> =
        withContext(Dispatchers.IO) {
            appRepo.getAllLiveOrders(pageSize).map { it.map { o -> o.toLibOrder() } }
        }

    override fun connectToOrders(channelName: String) = appRepo.connectToOrders(channelName)
    override fun disconnectFromOrders() = appRepo.disconnectFromOrders()
    override fun retryConnection() = appRepo.retryConnection()
    override fun orders(): StateFlow<List<LibOrder>> = state
}

class UpdateOrdersStatusRepositoryAdapter(
    private val update: suspend (orderId: String, statusId: Int, assignedAgentId: String?) -> LibOrderInfo
) : LibUpdateRepo {
    override suspend fun updateOrderStatus(
        orderId: String,
        statusId: Int,
        assignedAgentId: String?
    ): LibOrderInfo = update(orderId, statusId, assignedAgentId)
}

class DeviceLocationProviderAdapter(
    private val getDeviceLocations: GetDeviceLocationsUseCase
) : DeviceLocationProvider {
    override suspend fun getDeviceLocations(): Pair<Location?, Location?> {
        val (last, current) = getDeviceLocations()
        return last to current
    }
}

// Coordinates conversions
fun LibCoords.toApp(): AppCoords = AppCoords(lat = lat, lng = lng)
fun AppCoords.toLib(): LibCoords =
    LibCoords(lat = latitude, lng = longitude) // if your AppCoords is latitude/longitude names

// Markers conversion for your map
fun MapHost.Marker.toAppMarker(): MapMarker =
    MapMarker(
        id = id,
        title = title,
        coordinates = LibCoords(coords.lat, coords.lng).toApp(),
        distanceKm = distanceKm,
        snippet = snippet
    )

