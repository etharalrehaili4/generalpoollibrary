package com.ntg.lmd.mainscreen.ui.model

import com.ntg.core.location.location.domain.model.Coordinates
import com.ntg.core.location.location.domain.model.MapMarker
import com.ntg.core.location.location.domain.model.MapUiState
import com.ntg.lmd.mainscreen.domain.model.OrderInfo

data class GeneralPoolUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val orders: List<OrderInfo> = emptyList(),
    override val hasLocationPerm: Boolean = false,
    override val distanceThresholdKm: Double = 100.0,
    val searchText: String = "",
    val errorMessage: String? = null,
    val searching: Boolean = false,
    val selectedOrder: OrderInfo? = null,
) : MapUiState {
    companion object {
        private const val MAX_LATITUDE = 90.0
        private const val MAX_LONGITUDE = 180.0
    }

    override val markers: List<MapMarker>
        get() =
            orders
                .filter {
                    it.lat.isFinite() &&
                        it.lng.isFinite() &&
                        !(it.lat == 0.0 && it.lng == 0.0) &&
                        kotlin.math.abs(it.lat) <= MAX_LATITUDE &&
                        kotlin.math.abs(it.lng) <= MAX_LONGITUDE
                }.map {
                    MapMarker(
                        id = it.id,
                        title = it.name,
                        coordinates = Coordinates(it.lat, it.lng),
                        distanceKm = it.distanceKm,
                        snippet = it.orderNumber,
                    )
                }.let { base ->
                    if (!hasLocationPerm) return@let base
                    val anyFinite = base.any { it.distanceKm.isFinite() }
                    if (!anyFinite) return@let emptyList()
                    base.filter { it.distanceKm.isFinite() && it.distanceKm <= distanceThresholdKm }
                }

    override val selectedMarkerId: String?
        get() = selectedOrder?.id

    val filteredOrdersInRange: List<OrderInfo>
        get() {
            val q = searchText.trim()
            val base =
                orders.filter {
                    it.lat.isFinite() &&
                        it.lng.isFinite() &&
                        it.distanceKm.isFinite() &&
                        it.distanceKm <= distanceThresholdKm
                }
            if (q.isBlank()) return base
            return base.filter {
                it.orderNumber.contains(q, ignoreCase = true) ||
                    it.name.contains(q, ignoreCase = true)
            }
        }
}
