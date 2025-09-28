package com.example.myorderhistoryanddelivery.delivery.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myorderhistoryanddelivery.delivery.domain.model.DeliveryState
import com.example.myorderhistoryanddelivery.ui.theme.SuccessGreen

private val ICON_CELL = 48.dp

@Composable
fun statusCell(state: DeliveryState) {
    Box(Modifier.width(ICON_CELL), contentAlignment = Alignment.Center) {
        when (state) {
            DeliveryState.DELIVERED ->
                Icon(Icons.Filled.CheckCircle, null, tint = SuccessGreen)

            DeliveryState.CANCELLED, DeliveryState.FAILED ->
                Icon(Icons.Filled.Cancel, null, tint = MaterialTheme.colorScheme.error)

            else ->
                Icon(
                    Icons.Filled.CheckCircle,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
    }
}