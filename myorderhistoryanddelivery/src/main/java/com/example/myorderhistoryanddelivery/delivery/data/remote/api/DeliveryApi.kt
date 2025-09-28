package com.example.myorderhistoryanddelivery.delivery.data.remote.api

import com.example.myorderhistoryanddelivery.delivery.data.remote.dto.OrdersEnvelope
import retrofit2.http.GET
import retrofit2.http.Query

@Suppress("LongParameterList")
interface DeliveriesLogApi {
    @GET("orders-list")
    suspend fun getOrders(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("status_ids") statusIds: String? = null,
        @Query("search") search: String? = null,
        @Query("assigned_agent_id") assignedAgentId: String? = null,
        @Query("user_orders_only") userOrdersOnly: Boolean? = null,
    ): OrdersEnvelope
}
