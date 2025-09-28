package com.ntg.lmd.mainscreen.domain.model

import com.example.generalpool.models.OrderInfo

data class OrdersPage(
    val items: List<OrderInfo>,
    val rawCount: Int, // server page size BEFORE filtering
)
