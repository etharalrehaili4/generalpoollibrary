package com.ntg.core.location.location.domain.model

import androidx.compose.ui.unit.Dp
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState

data class MapMarker(
    val id: String,
    val title: String,
    val coordinates: Coordinates,
    val distanceKm: Double = 0.0,
    val snippet: String? = null,
)

interface MapUiState {
    val markers: List<MapMarker>
    val selectedMarkerId: String?
    val distanceThresholdKm: Double
    val hasLocationPerm: Boolean
}

data class MapChrome(
    val top: Dp,
    val bottom: Dp,
)

data class MapConfig(
    val ui: MapUiState,
    val mapStates: IMapStates,
    val deviceCoords: Coordinates?,
    val canShowMyLocation: Boolean,
)

interface IMapStates {
    fun move(update: MapCameraUpdate)
    suspend fun animate(update: MapCameraUpdate)
    fun updateMarker(coords: Coordinates)
}

fun IMapStates.asGoogleMapsStates(): Pair<CameraPositionState, MarkerState> {
    val google = this as GoogleMapStates
    return google.camera to google.marker
}

internal class GoogleMapStates(
    val camera: CameraPositionState,
    val marker: MarkerState,
) : IMapStates {

    override fun move(update: MapCameraUpdate) {
        camera.move(update.raw)
    }

    override suspend fun animate(update: MapCameraUpdate) {
        camera.animate(update.raw)
    }

    override fun updateMarker(coords: Coordinates) {
        marker.position = coords.toLatLng()
    }
}

