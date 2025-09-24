package com.ntg.core.location.location.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.ntg.core.location.location.domain.model.Coordinates
import com.ntg.core.location.location.domain.model.IMapStates
import com.ntg.core.location.location.domain.model.MapChrome
import com.ntg.core.location.location.domain.model.MapConfig
import com.ntg.core.location.location.domain.model.MapMarker
import com.ntg.core.location.location.domain.model.MapUiState
import com.ntg.core.location.location.domain.model.asGoogleMapsStates
import com.ntg.core.location.location.domain.model.toLatLng

private const val TOP_OVERLAY_RATIO = 0.09f // 9% of screen height
private const val BOTTOM_BAR_RATIO = 0.22f // 22% of screen height
private const val INITIAL_MAP_ZOOM = 8f

@Composable
fun mapScreen(
    ui: MapUiState,
    mapStates: IMapStates,
    deviceLatLng: Coordinates?,
    modifier: Modifier = Modifier,
    bottomOverlayPadding: Dp? = null,
) {
    val (cameraPositionState, _) = mapStates.asGoogleMapsStates()

    val (topDefault, bottomDefault) = overlayHeights()
    val chrome =
        remember(topDefault, bottomDefault, bottomOverlayPadding) {
            MapChrome(top = topDefault, bottom = bottomOverlayPadding ?: bottomDefault)
        }
    val canShowMyLocation = rememberCanShowMyLocation()

    var initialCentered by remember { mutableStateOf(false) }
    initialCenterEffect(deviceLatLng, cameraPositionState, initialCentered) { initialCentered = it }

    val config =
        remember(ui, mapStates, deviceLatLng, canShowMyLocation) {
            MapConfig(ui, mapStates, deviceLatLng, canShowMyLocation)
        }

    googleMapContent(config = config, chrome = chrome, modifier = modifier)
}

@Composable
private fun initialCenterEffect(
    deviceLatLng: Coordinates?,
    camera: CameraPositionState,
    alreadyCentered: Boolean,
    setCentered: (Boolean) -> Unit,
) {
    LaunchedEffect(deviceLatLng, alreadyCentered) {
        if (deviceLatLng != null && !alreadyCentered) {
            camera.animate(CameraUpdateFactory.newLatLngZoom(deviceLatLng.toLatLng(), INITIAL_MAP_ZOOM))
            setCentered(true)
        }
    }
}

@Composable
private fun googleMapContent(
    config: MapConfig,
    chrome: MapChrome,
    modifier: Modifier = Modifier,
) {
    val (cameraPositionState, markerState) = config.mapStates.asGoogleMapsStates()

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = config.canShowMyLocation),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true
        ),
        contentPadding = PaddingValues(top = chrome.top, bottom = chrome.bottom),
    ) {
        distanceCircle(
            deviceLatLng = config.deviceCoords,
            distanceKm = config.ui.distanceThresholdKm,
        )

        otherMarkers(
            markers = config.ui.markers,
            selectedMarkerId = config.ui.selectedMarkerId,
        )

        selectedMarkerPositionEffect(
            selected = config.ui.markers.find { it.id == config.ui.selectedMarkerId },
            markerState = markerState,
        )

        selectedMarker(
            selected = config.ui.markers.find { it.id == config.ui.selectedMarkerId },
            hasLocationPerm = config.ui.hasLocationPerm,
            thresholdKm = config.ui.distanceThresholdKm,
            markerState = markerState,
        )
    }
}

@Composable
private fun overlayHeights(): Pair<Dp, Dp> {
    val screenH = LocalConfiguration.current.screenHeightDp.dp
    val top = (screenH * TOP_OVERLAY_RATIO).coerceIn(48.dp, 96.dp)
    val bottom = (screenH * BOTTOM_BAR_RATIO).coerceIn(128.dp, 280.dp)
    return top to bottom
}

@Composable
private fun rememberCanShowMyLocation(): Boolean {
    val context = LocalContext.current
    val hasFine =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    val hasCoarse =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    return hasFine || hasCoarse
}

@Composable
private fun distanceCircle(
    deviceLatLng: Coordinates?,
    distanceKm: Double,
) {
    if (deviceLatLng != null && distanceKm > 0.0) {
        Circle(
            center = deviceLatLng.toLatLng(),
            radius = distanceKm * 1000.0,
            strokeWidth = 3f,
            strokeColor = MaterialTheme.colorScheme.primary,
            fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            zIndex = 0.5f,
        )
    }
}

@Composable
private fun otherMarkers(
    markers:List<MapMarker>,
    selectedMarkerId: String?,
) {
    markers.forEach { marker ->
        if (marker.id != selectedMarkerId) {
            val position = remember(marker.coordinates.latitude, marker.coordinates.longitude) { LatLng(marker.coordinates.latitude, marker.coordinates.longitude) }
            Marker(
                state = remember { MarkerState(position) },
                title = marker.title,
                snippet = marker.snippet,
                zIndex = 0f,
            )
        }
    }
}

@Composable
private fun selectedMarkerPositionEffect(
    selected: MapMarker?,
    markerState: MarkerState,
) {
    LaunchedEffect(selected?.coordinates?.latitude, selected?.coordinates?.longitude) {
        selected?.let { markerState.position = LatLng(it.coordinates.latitude, it.coordinates.longitude) }
    }
}

@Composable
private fun selectedMarker(
    selected: MapMarker?,
    hasLocationPerm: Boolean,
    thresholdKm: Double,
    markerState: MarkerState,
) {
    selected ?: return
    val withinRange =
        !hasLocationPerm || (selected.distanceKm.isFinite() && selected.distanceKm <= thresholdKm)

    if (withinRange) {
        Marker(
            state = markerState,
            title = selected.title,
            snippet = selected.snippet,
            zIndex = 1f,
        )
    }
}

