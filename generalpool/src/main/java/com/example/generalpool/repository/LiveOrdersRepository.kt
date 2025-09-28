package com.example.generalpool.repository

import com.example.generalpool.models.Order
import kotlinx.coroutines.flow.StateFlow

interface LiveOrdersRepository {
    suspend fun getAllLiveOrders(pageSize: Int): Result<List<Order>>

    fun connectToOrders(channelName: String = "orders")

    fun disconnectFromOrders()

    fun retryConnection()

    fun updateOrderStatus(
        orderId: String,
        status: String,
    )

    fun orders(): StateFlow<List<Order>>
}
