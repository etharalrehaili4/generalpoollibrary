package com.example.generalpool.domain.repository

import com.example.generalpool.domain.model.OrderInfo

interface UpdateOrdersStatusRepository {
    suspend fun updateOrderStatus(
        orderId: String,
        statusId: Int,
        assignedAgentId: String?,
    ): OrderInfo
}