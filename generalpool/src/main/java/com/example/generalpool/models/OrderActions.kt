package com.example.generalpool.models

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

sealed class OrderActions {
    data object Confirm : OrderActions()

    data object PickUp : OrderActions()

    data object Start : OrderActions()

    data object Deliver : OrderActions()

    data object Fail : OrderActions()
}

@Composable
fun statusTint(status: String) =
    if (status.equals("confirmed", ignoreCase = true) ||
        status.equals("added", ignoreCase = true)
    ) {
        Color(0xFF3BA864)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
