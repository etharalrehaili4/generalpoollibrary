package com.example.generalpool.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.generalpool.models.GeneralPoolUiState
import com.example.generalpool.models.OrderInfo
import com.ntg.horizontallist.GeneralHorizontalList
import com.ntg.horizontallist.GeneralHorizontalListCallbacks

@Composable
fun poolBottomContent(
    ui: GeneralPoolUiState,
    onOrderSelected: (OrderInfo) -> Unit,
    focusOnOrder: (OrderInfo, Boolean) -> Unit,
    onAddToMe: (OrderInfo) -> Unit,
) {
    when {
        ui.isLoading -> loadingText()
        ui.markers.isNotEmpty() ->
            ordersHorizontalList(
                ui = ui,
                focusOnOrder = focusOnOrder,
                onAddToMe = onAddToMe,
                onOrderSelected = onOrderSelected,
            )
    }
}

@Composable
fun loadingText() {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "loading",
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
fun ordersHorizontalList(
    ui: GeneralPoolUiState,
    focusOnOrder: (OrderInfo, Boolean) -> Unit,
    onAddToMe: (OrderInfo) -> Unit,
    onOrderSelected: (OrderInfo) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.align(Alignment.BottomCenter)) {
            GeneralHorizontalList(
                items = ui.filteredOrdersInRange,
                key = { it.orderNumber },
                callbacks =
                    GeneralHorizontalListCallbacks(
                        onCenteredItemChange = { order, _ ->
                            focusOnOrder(order, false)
                            onOrderSelected(order)
                        },
                    ),
                cardContent = { order, _ ->
                    orderCard(
                        order = order,
                        onAddClick = { onAddToMe(order) },
                        onOrderClick = { clicked -> focusOnOrder(clicked, false) },
                    )
                },
            )
        }
    }
}

