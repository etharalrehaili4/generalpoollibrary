package com.example.myorderhistoryanddelivery.order.domain.model

data class OrdersDialogsState(
    val showFilter: Boolean,
    val showSort: Boolean,
    val filter: OrdersHistoryFilter,
)