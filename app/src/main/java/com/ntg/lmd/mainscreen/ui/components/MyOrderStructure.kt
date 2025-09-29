package com.ntg.lmd.mainscreen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.lmdmypoolmyorder.domain.model.OrderInfo
import com.example.lmdmypoolmyorder.domain.model.OrderStatus
import com.example.lmdmypoolmyorder.screen.component.statusTint
import com.example.lmdmypoolmyorder.screen.viewmodel.UpdateOrderStatusViewModel
import com.ntg.lmd.R
import java.util.Locale

@Composable
fun distanceBadge(
    distanceKm: Double,
    modifier: Modifier = Modifier,
) {
    val bg = MaterialTheme.colorScheme.primary
    val fg = MaterialTheme.colorScheme.onPrimary
    Box(
        modifier =
            modifier
                .size(56.dp)
                .background(bg, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = distanceKm.let { String.format(Locale.getDefault(), "%.2f", it) },
                style = MaterialTheme.typography.labelSmall,
                color = fg,
            )
            Text(
                text = stringResource(R.string.unit_km),
                style = MaterialTheme.typography.labelSmall,
                color = fg.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
fun primaryActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.mediumSpace)),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun callButton(onCall: () -> Unit) {
    Button(
        onClick = onCall,
        modifier = Modifier.fillMaxWidth(),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_radius)),
    ) {
        Icon(
            imageVector = Icons.Filled.Phone,
            contentDescription = stringResource(R.string.call),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(dimensionResource(R.dimen.drawer_icon_size)),
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.smallerSpace)))
        Text(text = stringResource(R.string.call))
    }
}

@Composable
fun bottomStickyButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(R.dimen.mediumSpace),
                    vertical = dimensionResource(R.dimen.smallSpace),
                ),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(R.dimen.card_radius)),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        ) { Text(text = text, style = MaterialTheme.typography.titleMedium) }
    }
}

@Composable
fun orderHeaderWithMenu(
    order: OrderInfo,
    enabled: Boolean = true,
    onPickUp: () -> Unit,
    onCancel: () -> Unit,
    onReassign: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        orderHeaderLeft(
            order = order,
            onPickUp = onPickUp,
            onCancel = onCancel,
            onReassign = onReassign,
            enabled = enabled,
        )
    }
}

@Composable
fun orderHeaderLeft(
    order: OrderInfo,
    onPickUp: () -> Unit,
    onCancel: () -> Unit,
    onReassign: () -> Unit,
    enabled: Boolean = true,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        headerLeftInfo(order)
        headerRightInfo(
            order = order,
            enabled = enabled,
            menuState =
                MenuState(
                    expanded = menuExpanded,
                    onExpand = { menuExpanded = true },
                    onDismiss = { menuExpanded = false },
                    onPickUp = onPickUp,
                    onCancel = onCancel,
                    onReassign = onReassign,
                ),
        )
    }
}

@Composable
private fun headerLeftInfo(order: OrderInfo) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        distanceBadge(
            distanceKm = order.distanceKm,
            modifier = Modifier.padding(end = dimensionResource(R.dimen.mediumSpace)),
        )
        Column {
            Text(order.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "#${order.orderNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
            )
            Text(
                text = order.status.toString(),
                color = statusTint(order.status.toString()),
                style = MaterialTheme.typography.titleSmall,
            )
            order.details?.let {
                Spacer(Modifier.height(dimensionResource(R.dimen.extraSmallSpace)))
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun headerRightInfo(
    order: OrderInfo,
    enabled: Boolean,
    menuState: MenuState,
) {
    Column(horizontalAlignment = Alignment.End) {
        moreMenu(order = order, enabled = enabled, menuState = menuState)
        Spacer(Modifier.height(dimensionResource(R.dimen.smallerSpace)))
        order.price?.let { Text(it, style = MaterialTheme.typography.titleSmall) }
    }
}

@Composable
private fun moreMenu(
    order: OrderInfo,
    enabled: Boolean,
    menuState: MenuState,
) {
    IconButton(onClick = menuState.onExpand, modifier = Modifier.size(24.dp)) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.more_options),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
    DropdownMenu(expanded = menuState.expanded, onDismissRequest = menuState.onDismiss) {
        if (order.status == OrderStatus.CONFIRMED) {
            menuItems(order = order, enabled = enabled, menuState = menuState)
        }
    }
}

@Composable
private fun menuItems(
    order: OrderInfo,
    enabled: Boolean,
    menuState: MenuState,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.pick_order)) },
        enabled = enabled,
        onClick = {
            menuState.onDismiss()
            UpdateOrderStatusViewModel.OrderLogger.uiTap(order.id, order.orderNumber, "Menu:PickUp")
            menuState.onPickUp()
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.cancel_order)) },
        enabled = enabled, // status is CONFIRMED here
        onClick = {
            menuState.onDismiss()
            UpdateOrderStatusViewModel.OrderLogger.uiTap(order.id, order.orderNumber, "Menu:Cancel")
            menuState.onCancel()
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.reassign_order)) },
        enabled = enabled,
        onClick = {
            menuState.onDismiss()
            UpdateOrderStatusViewModel.OrderLogger.uiTap(
                order.id,
                order.orderNumber,
                "Menu:Reassign"
            )
            menuState.onReassign()
        },
    )
}

data class MenuState(
    val expanded: Boolean,
    val onExpand: () -> Unit,
    val onDismiss: () -> Unit,
    val onPickUp: () -> Unit,
    val onCancel: () -> Unit,
    val onReassign: () -> Unit,
)
