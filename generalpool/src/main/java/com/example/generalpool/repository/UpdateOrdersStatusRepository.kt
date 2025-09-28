package com.example.generalpool.repository

import com.example.generalpool.models.OrderInfo

interface UpdateOrdersStatusRepository {
    suspend fun updateOrderStatus(
        orderId: String,
        statusId: Int,
        assignedAgentId: String?,
    ): OrderInfo
}