package com.example.generalpool

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.generalpool.models.Coordinates
import com.example.generalpool.models.GeneralPoolUiState
import com.example.generalpool.models.MapHost
import com.example.generalpool.models.OrderInfo

@Composable
fun GeneralPoolScreen(
    state: GeneralPoolUiState,
    mapHost: MapHost,
    onFocusOnOrder: (OrderInfo, closeSearch: Boolean) -> Unit,
    onDistanceChange: (Double) -> Unit,
    onOrderSelected: (OrderInfo?) -> Unit,
    onAddToMe: (OrderInfo) -> Unit,
    deviceCoords: Coordinates?,
    // slots to let the app provide its own UI bits if it wants:
    distanceFilterBar: @Composable (maxDistanceKm: Double, onChange: (Double) -> Unit) -> Unit,
    searchDropdown: @Composable (visible: Boolean, orders: List<OrderInfo>, onPick: (OrderInfo) -> Unit) -> Unit,
    map: @Composable (markers: List<MapHost.Marker>, selectedId: String?, device: Coordinates?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        // Map
        val markers = remember(state.orders) {
            state.orders.map {
                MapHost.Marker(
                    id = it.id,
                    title = it.name,
                    coords = Coordinates(it.lat, it.lng),
                    distanceKm = it.distanceKm,
                    snippet = it.orderNumber
                )
            }
        }
        map(markers, state.selectedOrder?.id, deviceCoords)

        // Distance filter or hint
        if (state.hasLocationPerm) {
            Row(Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)) {
                Spacer(Modifier.weight(1f))
                distanceFilterBar(state.distanceThresholdKm, onDistanceChange)
                Spacer(Modifier.weight(1f))
            }
        } else {
            // App can overlay its own hint if needed (left minimal on purpose)
        }

        // Search dropdown
        searchDropdown(
            visible = state.searching && state.searchText.isNotBlank(),
            orders = state.orders.filter { it.distanceKm.isFinite() && it.distanceKm <= state.distanceThresholdKm },
        ) { picked ->
            onFocusOnOrder(picked, true)
        }

        // Bottom content (simplified): list/call-to-action area is app’s responsibility.
        // Expose selected order action:
        state.selectedOrder?.let { sel ->
            AssistChip(
                onClick = { onAddToMe(sel) },
                label = { Text("Add to me (${sel.orderNumber})") },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .padding(12.dp)
            )
        }
    }
}
