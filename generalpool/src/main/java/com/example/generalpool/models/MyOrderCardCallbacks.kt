package com.example.generalpool.models

data class MyOrderCardCallbacks(
    val onDetails: () -> Unit,
    val onCall: () -> Unit,
    val onAction: (OrderActions) -> Unit,
    val onReassignRequested: () -> Unit,
)
