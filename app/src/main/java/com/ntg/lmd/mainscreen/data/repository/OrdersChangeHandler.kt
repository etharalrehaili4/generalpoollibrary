package com.ntg.lmd.mainscreen.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ntg.lmd.mainscreen.data.model.Order
import com.ntg.network.sockets.ChangeHandler

class OrdersChangeHandler(
    private val gson: Gson,
    private val store: OrderStore
) : ChangeHandler {

    override fun onInsert(record: JsonObject) {
        val order = gson.fromJson(record, Order::class.java)
        store.add(order)
    }

    override fun onUpdate(record: JsonObject) {
        val order = gson.fromJson(record, Order::class.java)
        store.update(order)
    }

    override fun onDelete(id: String?, oldRecord: JsonObject?) {
        id?.let { store.remove(it) }
    }
}
