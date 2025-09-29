package com.example.generalpool.domain.usecase

import com.example.generalpool.data.models.Order
import com.example.generalpool.domain.repository.LiveOrdersRepository
import kotlinx.coroutines.flow.StateFlow

class OrdersRealtimeUseCase(
    private val repo: LiveOrdersRepository,
) {
    fun connect(channelName: String = "orders") = repo.connectToOrders(channelName)

    fun disconnect() = repo.disconnectFromOrders()

    fun retry() = repo.retryConnection()

    fun orders(): StateFlow<List<Order>> = repo.orders()
}