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

// --- Holders to keep the Composable API ≤ 5 params ---
data class GeneralPoolEnvironment(
    val deviceCoords: Coordinates?
)

data class GeneralPoolCallbacks(
    val onFocusOnOrder: (OrderInfo, closeSearch: Boolean) -> Unit,
    val onDistanceChange: (Double) -> Unit,
    val onOrderSelected: (OrderInfo?) -> Unit,
    val onAddToMe: (OrderInfo) -> Unit,
)

data class GeneralPoolSlots(
    val distanceFilterBar: @Composable (maxDistanceKm: Double, onChange: (Double) -> Unit) -> Unit,
    val searchDropdown: @Composable (visible: Boolean, orders: List<OrderInfo>, onPick: (OrderInfo) -> Unit) -> Unit,
    val map: @Composable (markers: List<MapHost.Marker>, selectedId: String?, device: Coordinates?) -> Unit,
)

// --- Public API: only 5 parameters ---
@Composable
fun GeneralPoolScreen(
    state: GeneralPoolUiState,
    env: GeneralPoolEnvironment,
    callbacks: GeneralPoolCallbacks,
    slots: GeneralPoolSlots,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        // Map markers
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

        // Map
        slots.map(markers, state.selectedOrder?.id, env.deviceCoords)

        // Distance filter (or hint space)
        if (state.hasLocationPerm) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Spacer(Modifier.weight(1f))
                slots.distanceFilterBar(state.distanceThresholdKm, callbacks.onDistanceChange)
                Spacer(Modifier.weight(1f))
            }
        } else {
            // host app may show its own overlay/hint if needed
        }

        // Search dropdown
        slots.searchDropdown.invoke(
            state.searching && state.searchText.isNotBlank(),
            state.orders.filter {
                it.distanceKm < Double.POSITIVE_INFINITY &&
                        !it.distanceKm.isNaN() &&
                        it.distanceKm <= state.distanceThresholdKm
            },
            { picked -> callbacks.onFocusOnOrder(picked, true) }
        )

        // Bottom CTA (example)
        state.selectedOrder?.let { sel ->
            AssistChip(
                onClick = { callbacks.onAddToMe(sel) },
                label = { Text("Add to me (${sel.orderNumber})") },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .padding(12.dp)
            )
        }
    }
}
