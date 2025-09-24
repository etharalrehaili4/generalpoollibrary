package com.ntg.core.location.location.domain.model

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory

data class MapCameraUpdate internal constructor(val raw: CameraUpdate)

fun cameraUpdateZoom(coords: Coordinates, zoom: Float): MapCameraUpdate {
    return MapCameraUpdate(
        CameraUpdateFactory.newLatLngZoom(
            coords.toLatLng(),
            zoom
        )
    )
}