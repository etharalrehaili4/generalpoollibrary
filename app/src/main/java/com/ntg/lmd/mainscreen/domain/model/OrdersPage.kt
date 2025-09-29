package com.ntg.lmd.mainscreen.domain.model

import com.example.lmdmypoolmyorder.domain.model.OrderInfo

data class OrdersPage(
    val items: List<OrderInfo>,
    val rawCount: Int, // server page size BEFORE filtering
)
