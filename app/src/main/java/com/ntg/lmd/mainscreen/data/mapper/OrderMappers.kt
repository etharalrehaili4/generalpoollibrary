package com.ntg.lmd.mainscreen.data.mapper

import com.example.generalpool.data.models.OrderDto
import com.example.generalpool.domain.model.OrderInfo
import com.example.generalpool.domain.model.OrderStatus.Companion.fromId
import com.example.generalpool.domain.model.RelativeTime
import com.ntg.lmd.utils.AppDefaults

private const val METERS_PER_KILOMETER = 1_000.0

private fun Double?.toKmOrDefault(default: Double): Double {
    val meters = this ?: return default
    return if (meters.isFinite() && meters >= 0.0) meters / METERS_PER_KILOMETER else default
}

fun OrderDto.toDomain(): OrderInfo =
    OrderInfo(
        id = orderId,
        name = customerName.orEmpty(),
        orderNumber = orderNumber,
        timeAgo = RelativeTime.JustNow,
        itemsCount = 0,
        distanceKm = distanceKm.toKmOrDefault(AppDefaults.DEFAULT_DISTANCE_KM),
        lat = coordinates?.latitude ?: AppDefaults.DEFAULT_LAT,
        lng = coordinates?.longitude ?: AppDefaults.DEFAULT_LNG,
        status = fromId(statusId),
        price = price,
        customerPhone = phone,
        customerId = customerId,
        assignedAgentId = assignedAgentId,
        details = null,
    )
