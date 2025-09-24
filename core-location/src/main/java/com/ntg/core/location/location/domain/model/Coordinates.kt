package com.ntg.core.location.location.domain.model

import com.google.android.gms.maps.model.LatLng

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

internal fun Coordinates.toLatLng(): LatLng = LatLng(latitude, longitude)

fun Coordinates.isValid(): Boolean =
    latitude.isFinite() && longitude.isFinite() && !(latitude == 0.0 && longitude == 0.0)
