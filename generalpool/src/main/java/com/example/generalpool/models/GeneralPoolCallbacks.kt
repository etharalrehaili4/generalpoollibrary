package com.example.generalpool.models

data class GeneralPoolCallbacks(
    val focusOnOrder: (OrderInfo, Boolean) -> Unit,
    val onAddToMe: (OrderInfo) -> Unit,
    val onOrderSelected: (OrderInfo) -> Unit,
    val onMaxDistanceKm: (Double) -> Unit,
)
