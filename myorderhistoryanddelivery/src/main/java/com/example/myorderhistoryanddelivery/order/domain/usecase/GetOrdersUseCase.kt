package com.example.myorderhistoryanddelivery.order.domain.usecase

import com.example.myorderhistoryanddelivery.order.domain.model.OrderHistoryUi
import com.example.myorderhistoryanddelivery.order.domain.repository.OrdersRepository

class GetOrdersUseCase(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(
        token: String,
        page: Int,
        limit: Int,
    ): List<OrderHistoryUi> = repository.getOrders(token, page, limit)
}