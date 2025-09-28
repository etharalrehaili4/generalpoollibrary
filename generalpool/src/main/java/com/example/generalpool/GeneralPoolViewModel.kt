package com.example.generalpool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.generalpool.models.Coordinates
import com.example.generalpool.models.GeneralPoolUiState
import com.example.generalpool.models.Order
import com.example.generalpool.models.OrderInfo
import com.example.generalpool.repository.LiveOrdersRepository
import com.example.generalpool.repository.UpdateOrdersStatusRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STATUS_ADDED = 1

class GeneralPoolViewModel(
    private val liveRepo: LiveOrdersRepository,
    private val updateRepo: UpdateOrdersStatusRepository,
    private val deviceLocation: DeviceLocationProvider,
) : ViewModel() {

    private val _ui = MutableStateFlow(GeneralPoolUiState())
    val ui: StateFlow<GeneralPoolUiState> = _ui

    private var realtimeStarted = false
    private var realtimeJob: Job? = null
    private var userPinnedSelection = false
    private var currentUserId: String? = null

    fun setCurrentUserId(id: String?) {
        currentUserId = id?.trim()?.ifEmpty { null }
    }

    fun attach() {
        if (!realtimeStarted) startRealtime()
        loadOrders()
    }

    override fun onCleared() {
        liveRepo.disconnectFromOrders()
        realtimeJob?.cancel()
        realtimeStarted = false
        super.onCleared()
    }

    fun onSearchingChange(v: Boolean) = _ui.update { it.copy(searching = v) }
    fun onSearchTextChange(v: String) = _ui.update { it.copy(searchText = v) }
    fun onOrderSelected(order: OrderInfo?) {
        userPinnedSelection = order != null
        _ui.update { it.copy(selectedOrder = order) }
    }

    fun onDistanceChange(km: Double) {
        _ui.update { it.copy(distanceThresholdKm = km) }
        ensureSelectionStillValid()
    }

    fun handleLocationPermission(granted: Boolean) {
        _ui.update { it.copy(hasLocationPerm = granted) }
        if (granted) refreshDistances()
    }

    fun addToMe(
        order: OrderInfo,
        userId: String,
        statusId: Int = STATUS_ADDED,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                updateRepo.updateOrderStatus(order.id, statusId, userId)
            }.onSuccess {
                // remove from pool locally
                _ui.update { s ->
                    s.copy(
                        orders = s.orders.filterNot { it.id == order.id },
                        selectedOrder = s.selectedOrder?.takeIf { it.id != order.id }
                    )
                }
                onSuccess()
            }.onFailure { e -> onError(e.message ?: "Failed to update order") }
        }
    }

    // ---- internals ----
    private fun startRealtime() {
        realtimeStarted = true
        liveRepo.connectToOrders("orders")
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            liveRepo.orders().collect { incoming ->
                val mapped = incoming.map { it.toUi() }.poolVisible(currentUserId)
                val merged = mergeByOrderNumber(_ui.value.orders, mapped).poolVisible(currentUserId)
                val nextSel =
                    determineSelection(merged, _ui.value.selectedOrder, userPinnedSelection)
                _ui.update { it.copy(orders = merged, selectedOrder = nextSel ?: it.selectedOrder) }
                if (_ui.value.hasLocationPerm) refreshDistances()
                ensureSelectionStillValid()
            }
        }
    }

    private fun loadOrders() {
        _ui.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            liveRepo.getAllLiveOrders(pageSize = 25)
                .onSuccess { all ->
                    val initial = all.map { it.toUi() }.poolVisible(currentUserId)
                    val def = initial.firstOrNull { it.lat != 0.0 || it.lng != 0.0 }
                        ?: initial.firstOrNull()
                    userPinnedSelection = false
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            orders = initial,
                            selectedOrder = def,
                            errorMessage = null
                        )
                    }
                    if (_ui.value.hasLocationPerm) refreshDistances()
                    ensureSelectionStillValid()
                }
                .onFailure {
                    _ui.update { s ->
                        s.copy(
                            isLoading = false,
                            errorMessage = "Unable to load orders"
                        )
                    }
                }
        }
    }

    private fun refreshDistances() {
        viewModelScope.launch {
            val (last, current) = deviceLocation.getDeviceLocations()
            val origin = current ?: last ?: return@launch
            val updated =
                applyDistances(Coordinates(origin.latitude, origin.longitude), _ui.value.orders)
            val nextSel =
                nextSelectionAfterDistance(_ui.value.selectedOrder, updated, userPinnedSelection)
            _ui.update { it.copy(orders = updated, selectedOrder = nextSel) }
            ensureSelectionStillValid()
        }
    }

    private fun ensureSelectionStillValid() {
        val s = _ui.value
        val sel = s.selectedOrder ?: return
        if (s.orders.none { it.orderNumber == sel.orderNumber }) {
            _ui.update { it.copy(selectedOrder = null) }
        }
    }
}

private fun Order.toUi(): OrderInfo = OrderInfo(
    id = orderId ?: id ?: orderNumber ?: "-",
    name = customerName ?: "-",
    orderNumber = orderNumber ?: orderId ?: id ?: "-",
    statusId = statusId,
    assignedAgentId = assignedAgentId,
    lat = latitude ?: 0.0,
    lng = longitude ?: 0.0,
    customerPhone = phone,
    details = address
)

private fun List<OrderInfo>.poolVisible(currentUserId: String?): List<OrderInfo> = filter { info ->
    val mine =
        currentUserId?.let { uid -> info.assignedAgentId?.equals(uid, true) == true } ?: false
    (info.statusId == 1 /* ADDED */) && !mine
}

private fun mergeByOrderNumber(
    existing: List<OrderInfo>,
    incoming: List<OrderInfo>
): List<OrderInfo> =
    if (existing.isEmpty()) incoming
    else if (incoming.isEmpty()) existing
    else {
        val map = existing.associateBy { it.orderNumber }.toMutableMap()
        for (o in incoming) map[o.orderNumber] = o
        map.values.toList()
    }

private fun determineSelection(
    merged: List<OrderInfo>,
    currentSel: OrderInfo?,
    pinned: Boolean
): OrderInfo? =
    when {
        pinned -> currentSel
        currentSel == null -> merged.firstOrNull { it.lat != 0.0 || it.lng != 0.0 }
            ?: merged.firstOrNull()

        merged.none { it.orderNumber == currentSel.orderNumber } -> null
        else -> merged.firstOrNull { it.orderNumber == currentSel.orderNumber }
    }

private fun nextSelectionAfterDistance(
    currentSel: OrderInfo?,
    updated: List<OrderInfo>,
    pinned: Boolean
): OrderInfo? {
    val nearest = updated.minByOrNull { it.distanceKm }
    val selectionHadNoDistance = currentSel?.distanceKm?.isFinite() != true
    return when {
        pinned -> currentSel
        currentSel == null -> nearest
        selectionHadNoDistance -> nearest
        else -> currentSel
    }
}

private fun applyDistances(origin: Coordinates, orders: List<OrderInfo>): List<OrderInfo> {
    fun haversineKm(a: Coordinates, b: Coordinates): Double {
        val R = 6371.0
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lng - a.lng)
        val la1 = Math.toRadians(a.lat)
        val la2 = Math.toRadians(b.lat)
        val h =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(la1) * Math.cos(la2) * Math.sin(dLon / 2) * Math.sin(
                dLon / 2
            )
        val c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h))
        return R * c
    }
    return orders
        .map { it.copy(distanceKm = haversineKm(origin, Coordinates(it.lat, it.lng))) }
        .sortedBy { it.distanceKm }
}
