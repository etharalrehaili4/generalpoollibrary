package com.ntg.lmd.mainscreen.ui.model

import com.ntg.lmd.mainscreen.domain.model.OrderInfo

data class GeneralPoolCallbacks(
    val focusOnOrder: (OrderInfo, Boolean) -> Unit,
    val onAddToMe: (OrderInfo) -> Unit,
    val onOrderSelected: (OrderInfo) -> Unit,
    val onMaxDistanceKm: (Double) -> Unit,
)
