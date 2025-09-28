package com.example.myorderhistoryanddelivery.order.domain.repository

import com.example.myorderhistoryanddelivery.order.domain.model.OrderHistoryUi

interface OrdersRepository {
    suspend fun getOrders(
        token: String,
        page: Int,
        limit: Int,
    ): List<OrderHistoryUi>
}
