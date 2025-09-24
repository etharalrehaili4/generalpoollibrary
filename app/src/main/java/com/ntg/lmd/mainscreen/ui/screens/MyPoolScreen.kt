package com.ntg.lmd.mainscreen.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ntg.core.location.location.domain.model.Coordinates
import com.ntg.core.location.location.domain.model.IMapStates
import com.ntg.core.location.location.domain.model.MapMarker
import com.ntg.core.location.location.domain.repository.LocationProvider
import com.ntg.core.location.location.screen.component.initialCameraPositionEffect
import com.ntg.core.location.location.screen.component.locationPermissionHandler
import com.ntg.core.location.location.screen.component.provideMapStates
import com.ntg.core.location.location.screen.component.rememberFocusOnMarker
import com.ntg.core.location.location.screen.mapScreen
import com.ntg.horizontallist.GeneralHorizontalList
import com.ntg.horizontallist.GeneralHorizontalListCallbacks
import com.ntg.lmd.R
import com.ntg.lmd.mainscreen.domain.model.OrderInfo
import com.ntg.lmd.mainscreen.ui.components.myPoolOrderCardItem
import com.ntg.lmd.mainscreen.ui.model.MyOrdersPoolUiState
import com.ntg.lmd.mainscreen.ui.viewmodel.MyPoolViewModel
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

// Zero fallback coordinates
private val ZERO_COORDS = Coordinates(0.0, 0.0)

private data class MapOverlayState(
    val isLoading: Boolean,
    val isLoadingMore: Boolean,
    val orders: List<OrderInfo>,
    val bottomPadding: Dp,
    val mapUi: MyOrdersPoolUiState,
    val mapStates: IMapStates,
)

private data class MapOverlayCallbacks(
    val onBottomHeightMeasured: (Int) -> Unit,
    val onCenteredOrderChange: (OrderInfo, Int) -> Unit,
    val onOpenOrderDetails: (String) -> Unit,
    val onNearEnd: (Int) -> Unit,
)

@Composable
fun myPoolScreen(
    viewModel: MyPoolViewModel = koinViewModel(),
    onOpenOrderDetails: (String) -> Unit,
) {
    val ui by viewModel.ui.collectAsState()
    var bottomBarHeight by remember { mutableStateOf(0.dp) }

    // decoupled location permission handling
    val context = LocalContext.current
    val locationProvider: LocationProvider = get()

    locationPermissionHandler(
        onPermissionGranted = {
            locationProvider.getLastKnownLocation(context) { coords ->
                viewModel.updateDeviceLocation(coords)
                Log.d("MyPoolScreen", "Got device coords = $coords")
            }
        },
        onPermissionDenied = {
            Log.w("MyPoolScreen", "Location permission denied")
        },
    )

    val mapStates = provideMapStates()
    initialCameraPositionEffect(ui.markers, ui.selectedMarkerId, mapStates)

    val state = overlayState(ui, bottomBarHeight, mapStates)
    val callbacks =
        rememberOverlayCallbacks(
            viewModel = viewModel,
            mapStates = mapStates,
            onOpenOrderDetails = onOpenOrderDetails,
            onNearEnd = { idx -> viewModel.loadNextIfNeeded(idx) },
            setBottomBarHeight = { bottomBarHeight = it },
        )

    mapWithBottomOverlay(state = state, callbacks = callbacks)
}

@Composable
private fun overlayState(
    ui: MyOrdersPoolUiState,
    bottomBarHeight: Dp,
    mapStates: IMapStates,
): MapOverlayState {
    val extra = dimensionResource(R.dimen.largeSpace)
    return MapOverlayState(
        isLoading = ui.isLoading,
        isLoadingMore = ui.isLoadingMore,
        orders = ui.orders,
        bottomPadding = bottomBarHeight + extra,
        mapUi = ui,
        mapStates = mapStates,
    )
}

@Composable
private fun rememberOverlayCallbacks(
    viewModel: MyPoolViewModel,
    mapStates: IMapStates,
    onOpenOrderDetails: (String) -> Unit,
    onNearEnd: (Int) -> Unit,
    setBottomBarHeight: (Dp) -> Unit,
): MapOverlayCallbacks {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val focus =
        rememberFocusOnMarker(
            onCenterChange = { marker ->
                // find matching order for marker
                val order =
                    viewModel.ui.value.orders
                        .firstOrNull { it.id == marker.id }
                order?.let { viewModel.onCenteredOrderChange(it) }
            },
            mapStates = mapStates,
            scope = scope,
        )

    return MapOverlayCallbacks(
        onBottomHeightMeasured = { px -> setBottomBarHeight(with(density) { px.toDp() }) },
        onCenteredOrderChange = { order, index ->
            val marker =
                MapMarker(
                    id = order.id,
                    title = order.name,
                    coordinates = Coordinates(order.lat, order.lng),
                    distanceKm = order.distanceKm,
                    snippet = order.orderNumber,
                )
            focus(marker)
            viewModel.onCenteredOrderChange(order, index)
        },
        onOpenOrderDetails = onOpenOrderDetails,
        onNearEnd = onNearEnd,
    )
}

@Composable
private fun mapWithBottomOverlay(
    state: MapOverlayState,
    callbacks: MapOverlayCallbacks,
) {
    Box(Modifier.fillMaxSize()) {
        mapScreen(
            ui = state.mapUi,
            mapStates = state.mapStates,
            deviceLatLng = ZERO_COORDS,
            bottomOverlayPadding = state.bottomPadding,
        )
        if (state.orders.isNotEmpty()) {
            bottomOverlay(state, callbacks)
        }
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun BoxScope.bottomOverlay(
    state: MapOverlayState,
    callbacks: MapOverlayCallbacks,
) {
    Column(
        Modifier
            .align(Alignment.BottomCenter)
            .onGloballyPositioned { callbacks.onBottomHeightMeasured(it.size.height) },
    ) {
        loadingMoreIndicator(state)
        ordersHorizontalList(state, callbacks)
    }
}

@Composable
private fun loadingMoreIndicator(state: MapOverlayState) {
    AnimatedVisibility(visible = state.isLoadingMore) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = dimensionResource(R.dimen.smallSpace)),
            )
        }
    }
}

@Composable
private fun ordersHorizontalList(
    state: MapOverlayState,
    callbacks: MapOverlayCallbacks,
) {
    GeneralHorizontalList(
        items = state.orders,
        key = { it.orderNumber }, // use a unique field from OrderInfo
        callbacks = GeneralHorizontalListCallbacks(
            onCenteredItemChange = { order, index ->
                callbacks.onCenteredOrderChange(order, index)
            },
            onNearEnd = { idx ->
                callbacks.onNearEnd(idx)
            }
        )
    ) { order, _ ->
        myPoolOrderCardItem(
            order = order,
            onOpenOrderDetails = callbacks.onOpenOrderDetails,
            onCall = { },
        )
    }
}
