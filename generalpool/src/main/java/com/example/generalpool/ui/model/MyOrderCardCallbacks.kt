package com.example.generalpool.ui.model

import com.example.generalpool.ui.components.OrderActions

data class MyOrderCardCallbacks(
    val onDetails: () -> Unit,
    val onCall: () -> Unit,
    val onAction: (OrderActions) -> Unit,
    val onReassignRequested: () -> Unit,
)