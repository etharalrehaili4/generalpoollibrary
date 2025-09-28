package com.example.generalpool.usecase

import com.example.generalpool.models.Order
import com.example.generalpool.repository.LiveOrdersRepository
import kotlinx.coroutines.flow.StateFlow

class OrdersRealtimeUseCase(
    private val repo: LiveOrdersRepository,
) {
    fun connect(channelName: String = "orders") = repo.connectToOrders(channelName)

    fun disconnect() = repo.disconnectFromOrders()

    fun retry() = repo.retryConnection()

    fun orders(): StateFlow<List<Order>> = repo.orders()
}
