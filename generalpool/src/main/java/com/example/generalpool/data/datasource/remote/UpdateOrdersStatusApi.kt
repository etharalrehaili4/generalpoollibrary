package com.example.generalpool.data.datasource.remote

import com.example.generalpool.data.models.UpdateOrderStatusEnvelope
import com.example.generalpool.data.models.UpdateOrderStatusRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface UpdatetOrdersStatusApi {
    @POST("update-order-status")
    suspend fun updateOrderStatus(
        @Body body: UpdateOrderStatusRequest,
    ): UpdateOrderStatusEnvelope
}
