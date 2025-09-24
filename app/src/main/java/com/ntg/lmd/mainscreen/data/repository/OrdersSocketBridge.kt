package com.ntg.lmd.mainscreen.data.repository

import android.util.Log
import com.google.gson.Gson
import com.ntg.network.sockets.SocketIntegration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrdersSocketBridge(
    private val socket: SocketIntegration,
    private val store: OrderStore,
    private val gson: Gson
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var listenerStarted = false

    // نفس الاسم الذي كان يستخدمه الريبو
    val orders: StateFlow<List<com.ntg.lmd.mainscreen.data.model.Order>> = store.state

    fun connect(channel: String) = socket.connect(channel)
    fun disconnect() = socket.disconnect()
    fun retryConnection() = socket.retryConnection()

    fun startChannelListener() {
        if (listenerStarted) return
        listenerStarted = true
        scope.launch {
            for (o in store.channel()) {
                Log.d("OrdersSocket", "ORDER: #${o.orderNumber} - ${o.customerName}")
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        val payload = mapOf("order_id" to orderId, "status" to status)
        socket.send(event = "UPDATE", payload = payload) // يستخدم send العامة في SocketIntegration
    }
}
