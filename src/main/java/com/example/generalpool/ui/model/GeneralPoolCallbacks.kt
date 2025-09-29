package com.example.generalpool.ui.model

import com.example.lmdmypoolmyorder.domain.model.OrderInfo

data class GeneralPoolCallbacks(
    val focusOnOrder: (OrderInfo, Boolean) -> Unit,
    val onAddToMe: (OrderInfo) -> Unit,
    val onOrderSelected: (OrderInfo) -> Unit,
    val onMaxDistanceKm: (Double) -> Unit,
)