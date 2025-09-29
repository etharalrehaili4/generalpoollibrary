package com.example.generalpool.domain.usecase

import com.example.generalpool.data.models.Order
import com.example.generalpool.domain.repository.LiveOrdersRepository

class LoadOrdersUseCase(
    private val repo: LiveOrdersRepository,
) {
    suspend operator fun invoke(pageSize: Int): Result<List<Order>> = repo.getAllLiveOrders(pageSize)
}