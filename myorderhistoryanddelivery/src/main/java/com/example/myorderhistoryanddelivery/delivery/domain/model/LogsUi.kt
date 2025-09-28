package com.example.myorderhistoryanddelivery.delivery.domain.model

data class LogsUi(
    val logs: List<DeliveryLog>,
    val loadingMore: Boolean,
    val endReached: Boolean,
    val refreshing: Boolean,
)