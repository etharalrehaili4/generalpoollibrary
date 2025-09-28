package com.example.myorderhistoryanddelivery.delivery.domain.repository

import com.example.myorderhistoryanddelivery.delivery.domain.model.DeliveryLog

interface DeliveriesLogRepository {
    suspend fun getLogsPage(
        page: Int,
        limit: Int,
        statusIds: List<Int>,
        search: String? = null,
    ): Pair<List<DeliveryLog>, Boolean>
}
