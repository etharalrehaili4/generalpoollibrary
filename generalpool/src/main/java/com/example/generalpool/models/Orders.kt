package com.example.generalpool.models

data class Coordinates(val lat: Double, val lng: Double)

data class OrderInfo(
    val id: String = "",
    val name: String = "",
    val orderNumber: String = "",
    val distanceKm: Double = Double.POSITIVE_INFINITY,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val statusId: Int? = null,
    val assignedAgentId: String? = null,
    val details: String? = null,
    val customerPhone: String? = null,
)

data class Order(
    val id: String? = null,
    val orderId: String? = null,
    val orderNumber: String? = null,
    val customerName: String? = null,
    val address: String? = null,
    val statusId: Int? = null,
    val assignedAgentId: String? = null,
    val price: String? = null,
    val phone: String? = null,
    val lastUpdated: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)