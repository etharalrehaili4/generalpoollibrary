package com.example.myorderhistoryanddelivery.delivery.domain.model

import com.example.myorderhistoryanddelivery.delivery.data.remote.dto.OrderDto
import com.example.myorderhistoryanddelivery.delivery.domain.usecase.DeliveryStatusIds


private fun mapState(statusId: Int?): DeliveryState =
    when (statusId) {
        DeliveryStatusIds.DELIVERED -> DeliveryState.DELIVERED
        DeliveryStatusIds.FAILED -> DeliveryState.FAILED
        DeliveryStatusIds.CANCELLED -> DeliveryState.CANCELLED
        else -> DeliveryState.OTHER
    }

fun OrderDto.toDeliveryLog(): DeliveryLog =
    DeliveryLog(
        orderId = "#$orderNumber",
        orderDate = orderDate.orEmpty(),
        deliveryTime = deliveryTime.orEmpty(),
        state = mapState(statusId),
    )
