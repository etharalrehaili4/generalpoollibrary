package com.example.generalpool.api

import com.example.generalpool.models.UpdateOrderStatusEnvelope
import com.example.generalpool.models.UpdateOrderStatusRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface UpdatetOrdersStatusApi {
    @POST("update-order-status")
    suspend fun updateOrderStatus(
        @Body body: UpdateOrderStatusRequest,
    ): UpdateOrderStatusEnvelope
}
