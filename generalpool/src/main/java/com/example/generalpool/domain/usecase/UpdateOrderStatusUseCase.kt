package com.example.generalpool.domain.usecase

import com.example.generalpool.domain.model.OrderInfo
import com.example.generalpool.domain.repository.UpdateOrdersStatusRepository

class UpdateOrderStatusUseCase(
    private val repo: UpdateOrdersStatusRepository,
) {
    suspend operator fun invoke(
        orderId: String,
        statusId: Int,
        assignedAgentId: String? = null,
    ): OrderInfo = repo.updateOrderStatus(orderId, statusId, assignedAgentId)
}