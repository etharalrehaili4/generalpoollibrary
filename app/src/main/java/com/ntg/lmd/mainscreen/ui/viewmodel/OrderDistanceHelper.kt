package com.ntg.lmd.mainscreen.ui.viewmodel

import com.ntg.core.location.location.domain.model.Coordinates
import com.ntg.lmd.mainscreen.domain.model.OrderInfo

object OrderDistanceHelper {
    fun applyDistances(
        origin: Coordinates?,
        orders: List<OrderInfo>,
        compute: (Coordinates, List<Coordinates>) -> List<Double>,
    ): List<OrderInfo> {
        if (origin == null) return orders

        val coords = orders.map { Coordinates(it.lat, it.lng) }
        val distances = compute(origin, coords)
        return orders
            .zip(distances) { order, dist -> order.copy(distanceKm = dist) }
            .sortedBy { it.distanceKm }
    }
}
