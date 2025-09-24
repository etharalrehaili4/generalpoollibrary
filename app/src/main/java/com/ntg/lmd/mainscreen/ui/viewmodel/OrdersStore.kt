package com.ntg.lmd.mainscreen.ui.viewmodel

import com.ntg.core.location.location.domain.model.Coordinates
import com.ntg.lmd.mainscreen.domain.model.OrderInfo
import com.ntg.lmd.mainscreen.ui.model.MyOrdersUiState
import kotlinx.coroutines.flow.MutableStateFlow

class OrdersStore(
    val state: MutableStateFlow<MyOrdersUiState>,
    val currentUserId: MutableStateFlow<String?>,
    val deviceLocation: MutableStateFlow<Coordinates?>,
    val allOrders: MutableList<OrderInfo> = mutableListOf(),
) {
    var page: Int = 1
    var endReached: Boolean = false
}
