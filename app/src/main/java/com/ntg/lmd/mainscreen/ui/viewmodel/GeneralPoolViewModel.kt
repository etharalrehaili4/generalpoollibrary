package com.ntg.lmd.mainscreen.ui.viewmodel

//import android.location.Location
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.generalpool.models.GeneralPoolUiState
//import com.example.generalpool.models.OrderInfo
//import com.example.generalpool.usecase.LoadOrdersUseCase
//import com.example.generalpool.usecase.OrdersRealtimeUseCase
//import com.ntg.core.location.location.domain.model.Coordinates
//import com.ntg.core.location.location.domain.usecase.ComputeDistancesUseCase
//import com.ntg.core.location.location.domain.usecase.GetDeviceLocationsUseCase
//import com.ntg.lmd.mainscreen.data.model.Order
//import com.ntg.lmd.mainscreen.ui.mapper.toUi
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asSharedFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
//sealed class GeneralPoolUiEvent {
//    data object RequestLocationPermission : GeneralPoolUiEvent()
//}
//
//class GeneralPoolViewModel(
//    private val ordersRealtime: OrdersRealtimeUseCase,
//    private val computeDistances: ComputeDistancesUseCase,
//    private val getDeviceLocations: GetDeviceLocationsUseCase,
//    private val loadOrdersUseCase: LoadOrdersUseCase,
//) : ViewModel() {
//    private val _ui = MutableStateFlow(GeneralPoolUiState())
//    val ui: StateFlow<GeneralPoolUiState> = _ui.asStateFlow()
//
//    private val _events = MutableSharedFlow<GeneralPoolUiEvent>(extraBufferCapacity = 1)
//    val events = _events.asSharedFlow()
//
//    private val _deviceCoordinates = MutableStateFlow<Coordinates?>(null)
//    val deviceCoordinates: StateFlow<Coordinates?> = _deviceCoordinates.asStateFlow()
//
//    private var realtimeStarted = false
//    private var realtimeJob: Job? = null
//
//    private var lastNonEmptyOrders: List<OrderInfo> = emptyList()
//    private var userPinnedSelection: Boolean = false
//    private var currentUserId: String? = null
//
//    val onSearchingChange: (Boolean) -> Unit = { v ->
//        _ui.update { it.copy(searching = v) }
//    }
//
//    val onSearchTextChange: (String) -> Unit = { v ->
//        _ui.update { it.copy(searchText = v) }
//    }
//
//    val onOrderSelected: (OrderInfo?) -> Unit = { order ->
//        userPinnedSelection = order != null
//        _ui.update { it.copy(selectedOrder = order) }
//    }
//
//    fun setCurrentUserId(id: String?) {
//        currentUserId = id?.trim()?.ifEmpty { null }
//    }
//
//    fun attach() {
//        if (!realtimeStarted) startRealtime()
//        loadOrdersFromApi()
//    }
//
//    override fun onCleared() {
//        ordersRealtime.disconnect()
//        realtimeJob?.cancel()
//        realtimeStarted = false
//        super.onCleared()
//    }
//
//    private fun startRealtime() {
//        realtimeStarted = true
//        ordersRealtime.connect("orders")
//        realtimeJob?.cancel()
//        realtimeJob =
//            viewModelScope.launch {
//                ordersRealtime.orders().collect { handleLiveOrders(it) }
//            }
//    }
//
//    private suspend fun handleLiveOrders(liveOrders: List<Order>) {
//        val incoming = liveOrders.map { it.toUi() }.poolVisible(currentUserId)
//        val merged = mergeOrders(_ui.value.orders, incoming).poolVisible(currentUserId)
//        val nextSel = determineNextSelection(merged, _ui.value.selectedOrder, userPinnedSelection)
//
//        _ui.update { it.copy(orders = merged, selectedOrder = nextSel ?: it.selectedOrder) }
//        if (merged.isNotEmpty()) lastNonEmptyOrders = merged
//
//        if (_ui.value.hasLocationPerm) {
//            viewModelScope.launch {
//                val (last, current) = getDeviceLocations()
//                val origin = current ?: last ?: return@launch
//                applyDistances(origin)
//            }
//        }
//
//        _ui.ensureSelectedStillVisible { removeInvalidSelectionIfNeeded() }
//    }
//
//    fun onDistanceChange(km: Double) {
//        _ui.update { it.copy(distanceThresholdKm = km) }
//        _ui.ensureSelectedStillVisible { removeInvalidSelectionIfNeeded() }
//    }
//
//    fun handleLocationPermission(
//        granted: Boolean,
//        promptIfMissing: Boolean = false,
//    ) {
//        _ui.update { it.copy(hasLocationPerm = granted) }
//        if (granted) {
//            viewModelScope.launch {
//                val (last, current) = getDeviceLocations()
//                val origin = current ?: last ?: return@launch
//                applyDistances(origin)
//            }
//        } else if (promptIfMissing) {
//            _events.tryEmit(GeneralPoolUiEvent.RequestLocationPermission)
//        }
//    }
//
//    private fun applyDistances(origin: Location) {
//        val coords = Coordinates(origin.latitude, origin.longitude)
//        val updated = applyDistancesToOrders(coords, _ui.value.orders)
//
//        val nextSelected =
//            determineSelectionAfterDistanceUpdate(
//                _ui.value.selectedOrder,
//                updated,
//                userPinnedSelection,
//            )
//
//        _ui.update(updateUiWithDistances(updated, nextSelected) { lastNonEmptyOrders = it })
//        _deviceCoordinates.value = coords
//        _ui.ensureSelectedStillVisible { this }
//    }
//
//    private fun loadOrdersFromApi() {
//        _ui.update { it.copy(isLoading = true) }
//        viewModelScope.launch {
//            val result = loadOrdersUseCase(pageSize = 25)
//            result
//                .onSuccess { allOrders ->
//                    val initial = allOrders.map { it.toUi() }.poolVisible(currentUserId)
//                    val defaultSel = pickDefaultSelection(_ui.value.selectedOrder, initial)
//                    userPinnedSelection = false
//                    _ui.update {
//                        it.copy(
//                            orders = initial,
//                            isLoading = false,
//                            selectedOrder = defaultSel,
//                            errorMessage = null,
//                        )
//                    }
//                    if (initial.isNotEmpty()) lastNonEmptyOrders = initial
//                    if (_ui.value.hasLocationPerm) {
//                        val (last, current) = getDeviceLocations()
//                        val origin = current ?: last
//                        if (origin != null) applyDistances(origin)
//                    }
//                    _ui.ensureSelectedStillVisible { removeInvalidSelectionIfNeeded() }
//                }.onFailure {
//                    Log.e("GeneralPoolVM", "Failed to load orders: ${it.message}", it)
//                    _ui.update { s ->
//                        s.copy(isLoading = false, errorMessage = "Unable to load orders.")
//                    }
//                }
//        }
//    }
//
//    fun removeOrderFromPool(orderId: String) {
//        _ui.update { cur ->
//            val newOrders = cur.orders.filterNot { it.id == orderId }
//            cur.copy(
//                orders = newOrders,
//                selectedOrder = cur.selectedOrder?.takeIf { it.id != orderId },
//            )
//        }
//    }
//
//    private fun applyDistancesToOrders(
//        origin: Coordinates,
//        orders: List<OrderInfo>,
//    ): List<OrderInfo> {
//        val coords = orders.map { Coordinates(it.lat, it.lng) }
//        val distances = computeDistances.computeDistances(origin, coords)
//        return orders
//            .zip(distances) { order, dist -> order.copy(distanceKm = dist) }
//            .sortedBy { it.distanceKm }
//    }
//
//    private fun List<OrderInfo>.poolVisible(currentUserId: String?): List<OrderInfo> =
//        filter { info ->
//            val mine =
//                currentUserId?.let { uid ->
//                    info.assignedAgentId?.equals(uid, ignoreCase = true) == true
//                } ?: false
//            info.status == OrderStatus.ADDED && !mine
//        }
//}
