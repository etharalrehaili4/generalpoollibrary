package com.example.generalpool.models

interface MapHost {
    data class Marker(
        val id: String,
        val title: String,
        val coords: Coordinates,
        val distanceKm: Double,
        val snippet: String? = null
    )

    @JvmInline
    value class CameraZoom(val value: Float)

    fun cameraUpdate(coords: Coordinates, zoom: CameraZoom): Any
    suspend fun move(update: Any)
    suspend fun animate(update: Any)
    fun updateSelectedMarker(coords: Coordinates)
}