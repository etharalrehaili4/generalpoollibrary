package com.example.generalpool.models

data class GeneralPoolUiState(
    val isLoading: Boolean = false,
    val orders: List<OrderInfo> = emptyList(),
    val hasLocationPerm: Boolean = false,
    val distanceThresholdKm: Double = 100.0,
    val searchText: String = "",
    val searching: Boolean = false,
    val selectedOrder: OrderInfo? = null,
    val errorMessage: String? = null,
)
