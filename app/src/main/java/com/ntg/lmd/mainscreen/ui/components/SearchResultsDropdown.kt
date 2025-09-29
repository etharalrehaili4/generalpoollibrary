package com.ntg.lmd.mainscreen.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.generalpool.domain.model.OrderInfo
import com.ntg.lmd.R

@Composable
fun searchResultsDropdown(
    visible: Boolean,
    orders: List<OrderInfo>,
    onPick: (OrderInfo) -> Unit,
) {
    AnimatedVisibility(
        visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp,
        ) {
            dropdownContent(orders = orders, onPick = onPick)
        }
    }
}

@Composable
private fun dropdownContent(
    orders: List<OrderInfo>,
    onPick: (OrderInfo) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        if (orders.isEmpty()) {
            noResultsView()
        } else {
            orders.forEach { order ->
                searchResultItem(order = order, onPick = onPick)
            }
        }
    }
}

@Composable
private fun noResultsView() {
    Text(
        text = stringResource(R.string.no_orders),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun searchResultItem(
    order: OrderInfo,
    onPick: (OrderInfo) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onPick(order) }
                .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = "${order.orderNumber} • ${order.name}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
