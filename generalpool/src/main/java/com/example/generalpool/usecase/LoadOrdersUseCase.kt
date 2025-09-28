package com.example.generalpool.usecase

import com.example.generalpool.models.Order
import com.example.generalpool.repository.LiveOrdersRepository

class LoadOrdersUseCase(
    private val repo: LiveOrdersRepository,
) {
    suspend operator fun invoke(pageSize: Int): Result<List<Order>> = repo.getAllLiveOrders(pageSize)
}
