package com.ntg.lmd.mainscreen.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.ntg.core.location.location.domain.repository.LocationProvider
import com.ntg.core.location.location.screen.component.initialCameraPositionEffect
import com.ntg.core.location.location.screen.component.locationPermissionHandler
import com.ntg.core.location.location.screen.component.provideMapStates
import com.ntg.lmd.R
import com.ntg.lmd.mainscreen.domain.model.OrderStatus
import com.ntg.lmd.mainscreen.domain.model.toMapMarker
import com.ntg.lmd.mainscreen.ui.components.OrdersContentCallbacks
import com.ntg.lmd.mainscreen.ui.components.OrdersContentDeps
import com.ntg.lmd.mainscreen.ui.components.bottomStickyButton
import com.ntg.lmd.mainscreen.ui.components.ordersContent
import com.ntg.lmd.mainscreen.ui.components.ordersEffects
import com.ntg.lmd.mainscreen.ui.components.reassignBottomSheet
import com.ntg.lmd.mainscreen.ui.model.MyOrdersPoolUiState
import com.ntg.lmd.mainscreen.ui.model.MyOrdersUiState
import com.ntg.lmd.mainscreen.ui.viewmodel.ActiveAgentsViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.AgentsState
import com.ntg.lmd.mainscreen.ui.viewmodel.MyOrdersViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.MyPoolViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.UpdateOrderStatusViewModel
import com.ntg.lmd.mainscreen.ui.viewmodel.UpdateOrderStatusViewModel.OrderLogger
import com.ntg.lmd.utils.SecureUserStore
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun myOrdersScreen(
    navController: NavController,
    onOpenOrderDetails: (String) -> Unit,
) {
    val ordersVm: MyOrdersViewModel = koinViewModel()
    val updateVm: UpdateOrderStatusViewModel = koinViewModel()
    val agentsVm: ActiveAgentsViewModel = koinViewModel()
    val poolVm: MyPoolViewModel = koinViewModel()

    val snack = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val reassignOrderId = remember { mutableStateOf<String?>(null) }

    wireMyOrders(
        WireDeps(
            navController = navController,
            ordersVm = ordersVm,
            updateVm = updateVm,
            agentsVm = agentsVm,
            poolVm = poolVm,
            listState = listState,
            snack = snack,
            reassignOrderId = reassignOrderId,
        ),
    )

    ordersBody(
        OrdersBodyDeps(
            ordersVm = ordersVm,
            updateVm = updateVm,
            agentsVm = agentsVm,
            listState = listState,
            snack = snack,
            reassignOrderId = reassignOrderId,
            onOpenOrderDetails = onOpenOrderDetails,
        ),
    )
}

@Composable
private fun ordersBody(deps: OrdersBodyDeps) {
    val uiState by deps.ordersVm.uiState.collectAsState()
    val updatingIds by deps.updateVm.updatingIds.collectAsState()
    val agentsState by deps.agentsVm.state.collectAsState()

    ordersScaffold(
        deps = deps,
        updatingIds = updatingIds,
    )

    reassignSheet(
        deps = deps,
        uiOrders = uiState,
        agentsState = agentsState,
    )
}

@Composable
private fun ordersScaffold(
    deps: OrdersBodyDeps,
    updatingIds: Set<String>,
) {
    val listState = deps.listState
    val snack = deps.snack

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = { bottomStickyButton(text = stringResource(R.string.order_pool)) {} },
    ) { innerPadding ->
        ordersContent(
            ordersVm = deps.ordersVm,
            deps =
                OrdersContentDeps(
                    updateVm = deps.updateVm,
                    listState = listState,
                    updatingIds = updatingIds,
                ),
            cbs =
                OrdersContentCallbacks(
                    onOpenOrderDetails = deps.onOpenOrderDetails,
                    onReassignRequested = { id ->
                        deps.reassignOrderId.value = id
                        deps.agentsVm.load()
                    },
                ),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}

@Composable
private fun reassignSheet(
    deps: OrdersBodyDeps,
    uiOrders: MyOrdersUiState,
    agentsState: AgentsState,
) {
    reassignBottomSheet(
        open = deps.reassignOrderId.value != null,
        state = agentsState,
        onDismiss = { deps.reassignOrderId.value = null },
        onRetry = { deps.agentsVm.load() },
        onSelect = { user ->
            val orderId = deps.reassignOrderId.value ?: return@reassignBottomSheet
            android.util.Log.d("ReassignFlow", "onSelect: orderId=$orderId → newAssignee=${user.id}")

            OrderLogger.uiTap(
                orderId,
                uiOrders.orders.firstOrNull { it.id == orderId }?.orderNumber,
                "Menu:Reassign→${user.name}",
            )
            deps.ordersVm.statusVM.updateStatusLocally(
                id = orderId,
                newStatus = OrderStatus.ADDED,
                newAssigneeId = user.id,
            )
            deps.updateVm.update(
                orderId = orderId,
                targetStatus = OrderStatus.ADDED,
                assignedAgentId = user.id,
            )
            deps.reassignOrderId.value = null
        },
    )
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
private fun wireMyOrders(deps: WireDeps) {
    val ctx = LocalContext.current
    val poolUi by deps.poolVm.ui.collectAsState()

    myOrdersLocationSection(deps, poolUi)
    myOrdersUserSection(deps)
    myOrdersEffectsSection(deps, ctx)
    observeOrdersSearch(deps.navController, deps.ordersVm)
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun myOrdersLocationSection(
    deps: WireDeps,
    poolUi: MyOrdersPoolUiState,
) {
    val locationProvider: LocationProvider = get() // from Koin
    val context = LocalContext.current

    locationPermissionHandler(
        onPermissionGranted = {
            locationProvider.getLastKnownLocation(context) { coords ->
                coords?.let { deps.poolVm.updateDeviceLocation(it) }
            }
        },
    )

    val markers = poolUi.orders.map { it.toMapMarker() }
    val mapStates = provideMapStates()

    initialCameraPositionEffect(
        markers = markers,
        selectedMarkerId = poolUi.selectedOrderNumber,
        mapStates = mapStates,
    )

    forwardMyPoolLocationToMyOrders(deps.poolVm, deps.ordersVm)
}

@Composable
private fun myOrdersUserSection(deps: WireDeps) {
    val userStore: SecureUserStore = koinInject()
    val currentUserId: String? = remember { userStore.getUserId() }
    LaunchedEffect(currentUserId) {
        deps.ordersVm.listVM.setCurrentUserId(currentUserId)
    }
}

@Composable
private fun myOrdersEffectsSection(
    deps: WireDeps,
    ctx: android.content.Context,
) {
    ordersEffects(
        vm = deps.ordersVm,
        updateVm = deps.updateVm,
        listState = deps.listState,
        snackbarHostState = deps.snack,
        context = ctx,
    )

    val uiState by deps.ordersVm.uiState.collectAsState()
    LaunchedEffect(uiState.query) { deps.listState.scrollToItem(0) }

    LaunchedEffect(Unit) {
        deps.updateVm.error.collect { (msg, retry) ->
            val res =
                deps.snack.showSnackbar(
                    message = msg,
                    actionLabel = ctx.getString(R.string.retry),
                    withDismissAction = true,
                )
            if (res == SnackbarResult.ActionPerformed) retry()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
private fun forwardMyPoolLocationToMyOrders(
    poolVm: MyPoolViewModel,
    ordersVm: MyOrdersViewModel,
) {
    val lastLoc by poolVm.lastLocation.collectAsState(initial = null)
    LaunchedEffect(lastLoc) { ordersVm.listVM.updateDeviceLocation(lastLoc) }
}

// Handle search for orders
@Composable
private fun observeOrdersSearch(
    navController: NavController,
    vm: MyOrdersViewModel,
) {
    val back = navController.currentBackStackEntry

    // Launched Effects for Search
    LaunchedEffect(back) {
        val h = back?.savedStateHandle ?: return@LaunchedEffect
        combine(
            h.getStateFlow("searching", false),
            h.getStateFlow("search_text", ""),
        ) { enabled, text -> if (enabled) text else null }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { q ->
                vm.searchVM.applySearchQuery(q)
            }
    }

    LaunchedEffect(back) {
        val h = back?.savedStateHandle ?: return@LaunchedEffect
        h.getStateFlow("search_submit", "").collect { submitted ->
            if (submitted.isNotEmpty()) {
                vm.searchVM.applySearchQuery(submitted)
                h["search_submit"] = ""
            }
        }
    }
}

data class OrdersBodyDeps(
    val ordersVm: MyOrdersViewModel,
    val updateVm: UpdateOrderStatusViewModel,
    val agentsVm: ActiveAgentsViewModel,
    val listState: LazyListState,
    val snack: SnackbarHostState,
    val reassignOrderId: MutableState<String?>,
    val onOpenOrderDetails: (String) -> Unit,
)

data class WireDeps(
    val navController: NavController,
    val ordersVm: MyOrdersViewModel,
    val updateVm: UpdateOrderStatusViewModel,
    val agentsVm: ActiveAgentsViewModel,
    val poolVm: MyPoolViewModel,
    val listState: LazyListState,
    val snack: SnackbarHostState,
    val reassignOrderId: MutableState<String?>,
)
