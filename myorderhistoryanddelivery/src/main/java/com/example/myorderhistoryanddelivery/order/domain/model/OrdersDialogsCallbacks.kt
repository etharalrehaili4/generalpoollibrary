package com.example.myorderhistoryanddelivery.order.domain.model

data class OrdersDialogsCallbacks(
    val onFilterDismiss: () -> Unit,
    val onSortDismiss: () -> Unit,
    val onApplyFilter: (Set<OrderHistoryStatus>) -> Unit,
    val onApplySort: (Boolean) -> Unit,
)
