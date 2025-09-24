package com.ntg.core.location.location.screen.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.ntg.core.location.location.domain.model.GoogleMapStates
import com.ntg.core.location.location.domain.model.IMapStates

@Composable
fun provideMapStates(): IMapStates {
    val camera = rememberCameraPositionState()
    val marker = remember { MarkerState(LatLng(0.0, 0.0)) }
    return GoogleMapStates(camera, marker)
}
