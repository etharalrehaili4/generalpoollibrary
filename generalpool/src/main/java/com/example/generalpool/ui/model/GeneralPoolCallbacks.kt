package com.example.generalpool.ui.model

import com.example.generalpool.domain.model.OrderInfo

data class GeneralPoolCallbacks(
    val focusOnOrder: (OrderInfo, Boolean) -> Unit,
    val onAddToMe: (OrderInfo) -> Unit,
    val onOrderSelected: (OrderInfo) -> Unit,
    val onMaxDistanceKm: (Double) -> Unit,
)