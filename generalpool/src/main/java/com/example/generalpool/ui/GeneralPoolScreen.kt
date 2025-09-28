package com.example.generalpool.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.generalpool.models.GeneralPoolCallbacks
import com.example.generalpool.models.GeneralPoolUiState
import com.example.generalpool.models.OrderInfo
import com.example.generalpool.models.OrderStatus
import com.example.generalpool.vm.GeneralPoolViewModel
import com.example.generalpool.vm.UpdateOrderStatusViewModel
import com.ntg.core.location.location.domain.model.Coordinates
import com.ntg.core.location.location.domain.model.IMapStates
import com.ntg.core.location.location.domain.model.cameraUpdateZoom
import com.ntg.core.location.location.screen.component.locationPermissionHandler
import com.ntg.core.location.location.screen.component.provideMapStates
import com.ntg.core.location.location.screen.mapScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

// Map / Camera behavior
private const val INITIAL_MAP_ZOOM = 12f
private const val ORDER_FOCUS_ZOOM = 14f

@Composable
fun generalPoolScreen(
    navController: NavController,
    generalPoolViewModel: GeneralPoolViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val ui by generalPoolViewModel.ui.collectAsStateWithLifecycle()
    val mapStates = provideMapStates()
    val scope = rememberCoroutineScope()
    val deviceCoords by generalPoolViewModel.deviceCoordinates.collectAsStateWithLifecycle()
    val hasCenteredOnDevice = remember { mutableStateOf(false) }
    val userStore: SecureUserStore = koinInject()

    // Extracted setup
    setupGeneralPool(
        viewModel = generalPoolViewModel,
        deviceCoords = deviceCoords,
        hasCenteredOnDevice = hasCenteredOnDevice,
        mapStates = mapStates,
    )

    // Extracted permissions
    generalPoolPermissions(viewModel = generalPoolViewModel)

    // Extracted search effects
    rememberSearchEffects(navController, generalPoolViewModel)

    val focusOnOrder =
        rememberFocusOnOrder(
            viewModel = generalPoolViewModel,
            mapStates = mapStates,
            scope = scope,
        )
    val onAddToMe = addToMeAction(context, generalPoolViewModel, userStore.getUserId())

    generalPoolLayout(
        ui = ui,
        mapStates = mapStates,
        deviceCoords = deviceCoords,
        callbacks =
            GeneralPoolCallbacks(
                focusOnOrder = focusOnOrder,
                onAddToMe = onAddToMe,
                onOrderSelected = generalPoolViewModel.onOrderSelected,
                onMaxDistanceKm = generalPoolViewModel::onDistanceChange,
            ),
    )
}

@Composable
private fun setupGeneralPool(
    viewModel: GeneralPoolViewModel,
    deviceCoords: Coordinates?,
    hasCenteredOnDevice: MutableState<Boolean>,
    mapStates: IMapStates,
) {
    val userStore: SecureUserStore = koinInject()
    val currentUserId = remember { userStore.getUserId() }
    setupInitialCamera(
        viewModel.ui.collectAsState().value,
        deviceCoords,
        mapStates,
        hasCenteredOnDevice
    )

    LaunchedEffect(Unit) {
        viewModel.setCurrentUserId(currentUserId)
        viewModel.attach()
    }
}

@Composable
private fun generalPoolPermissions(viewModel: GeneralPoolViewModel) {
    locationPermissionHandler(
        onPermissionGranted = { viewModel.handleLocationPermission(true) },
        onPermissionDenied = { viewModel.handleLocationPermission(false, promptIfMissing = true) },
    )
}

@Composable
private fun generalPoolLayout(
    ui: GeneralPoolUiState,
    mapStates: IMapStates,
    deviceCoords: Coordinates?,
    callbacks: GeneralPoolCallbacks,
) {
    Box(Modifier.fillMaxSize()) {
        generalPoolContent(
            ui = ui,
            focusOnOrder = callbacks.focusOnOrder,
            onMaxDistanceKm = callbacks.onMaxDistanceKm,
            mapStates = mapStates,
            deviceLatLng = deviceCoords,
        )
        poolBottomContent(
            ui = ui,
            onOrderSelected = callbacks.onOrderSelected,
            focusOnOrder = callbacks.focusOnOrder,
            onAddToMe = callbacks.onAddToMe,
        )
    }
}

@Composable
private fun addToMeAction(
    context: Context,
    viewModel: GeneralPoolViewModel,
    currentUserId: String?,
): (OrderInfo) -> Unit {
    val updateVm: UpdateOrderStatusViewModel = koinViewModel()
    val scope = rememberCoroutineScope()

    return remember(currentUserId) {
        { order ->
            val uid = currentUserId
            if (uid.isNullOrBlank()) return@remember
            val status = order.status ?: OrderStatus.ADDED
            viewModel.onOrderSelected(order.copy(assignedAgentId = uid))
            scope.launch {
                runCatching {
                    updateVm.update(
                        orderId = order.id,
                        targetStatus = status,
                        assignedAgentId = uid,
                    )
                }.onSuccess {
                    viewModel.onOrderSelected(null)
                    viewModel.removeOrderFromPool(order.id)
                    Toast.makeText(context, "Order Added Successfully", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Failed to add order", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
private fun generalPoolContent(
    ui: GeneralPoolUiState,
    focusOnOrder: (OrderInfo, Boolean) -> Unit,
    onMaxDistanceKm: (Double) -> Unit,
    mapStates: IMapStates,
    deviceLatLng: Coordinates?,
) {
    Box(Modifier.fillMaxSize()) {
        mapScreen(
            ui = ui,
            mapStates = mapStates,
            deviceLatLng = deviceLatLng,
            modifier = Modifier.fillMaxSize(),
        )
        distanceFilterOrHint(ui, onMaxDistanceKm)
        if (ui.searching && ui.searchText.isNotBlank()) {
            searchDropdown(ui, focusOnOrder)
        }
    }
}

@Composable
private fun distanceFilterOrHint(
    ui: GeneralPoolUiState,
    onMaxDistanceKm: (Double) -> Unit,
) {
    if (ui.hasLocationPerm) {
        distanceFilterRow(ui, onMaxDistanceKm)
    } else {
        locationAccessHint()
    }
}

@Composable
private fun distanceFilterRow(
    ui: GeneralPoolUiState,
    onMaxDistanceKm: (Double) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .zIndex(1f),
        horizontalArrangement = Arrangement.Center,
    ) {
        distanceFilterBar(
            maxDistanceKm = ui.distanceThresholdKm,
            onMaxDistanceKm = onMaxDistanceKm,
            enabled = true,
        )
    }
}

@Composable
private fun locationAccessHint() {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "location access request",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .zIndex(1f),
        )
    }
}

@Composable
private fun searchDropdown(
    ui: GeneralPoolUiState,
    focusOnOrder: (OrderInfo, Boolean) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .zIndex(2f),
        ) {
            searchResultsDropdown(
                visible = true,
                orders = ui.filteredOrdersInRange,
                onPick = { focusOnOrder(it, true) },
            )
        }
    }
}

@Composable
private fun rememberSearchEffects(
    navController: NavController,
    viewModel: GeneralPoolViewModel,
) {
    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("searching", false).collect { searching ->
            viewModel.onSearchingChange(searching)
            if (!searching) viewModel.onSearchTextChange("")
        }
    }

    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("search_text", "").collect { text ->
            viewModel.onSearchTextChange(text)
        }
    }

    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("search_submit", "").collect { /* ignore */ }
    }
}

@Composable
private fun setupInitialCamera(
    ui: GeneralPoolUiState,
    deviceCoords: Coordinates?,
    mapStates: IMapStates,
    hasCenteredOnDevice: MutableState<Boolean>,
) {
    LaunchedEffect(deviceCoords, ui.selectedMarkerId) {
        if (deviceCoords != null && ui.selectedMarkerId == null && !hasCenteredOnDevice.value) {
            mapStates.move(cameraUpdateZoom(deviceCoords, INITIAL_MAP_ZOOM))
            hasCenteredOnDevice.value = true
        }
    }
}

@Composable
fun rememberFocusOnOrder(
    viewModel: GeneralPoolViewModel,
    mapStates: IMapStates,
    scope: CoroutineScope,
    focusZoom: Float = ORDER_FOCUS_ZOOM,
): (OrderInfo, Boolean) -> Unit {
    val vm = rememberUpdatedState(viewModel)

    return remember {
        { order: OrderInfo, closeSearch: Boolean ->
            vm.value.onOrderSelected(order)

            val coords = Coordinates(order.lat, order.lng)
            mapStates.updateMarker(coords)

            scope.launch {
                mapStates.animate(cameraUpdateZoom(coords, focusZoom))
            }

            if (closeSearch) {
                vm.value.onSearchingChange(false)
                vm.value.onSearchTextChange("")
            }
        }
    }
}