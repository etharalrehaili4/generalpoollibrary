package com.ntg.lmd.mainscreen.ui.viewmodel

import com.ntg.lmd.mainscreen.domain.model.OrderInfo
import com.ntg.lmd.mainscreen.domain.model.OrderStatus
import com.ntg.lmd.mainscreen.ui.model.GeneralPoolUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal fun mergeOrders(
    existing: List<OrderInfo>,
    incoming: List<OrderInfo>,
): List<OrderInfo> =
    when {
        existing.isEmpty() -> incoming
        incoming.isEmpty() -> existing
        else -> {
            val map = existing.associateBy { it.orderNumber }.toMutableMap()
            for (o in incoming) map[o.orderNumber] = o
            map.values.toList()
        }
    }

internal fun determineNextSelection(
    merged: List<OrderInfo>,
    currentSel: OrderInfo?,
    userPinnedSelection: Boolean,
): OrderInfo? =
    when {
        userPinnedSelection -> currentSel
        currentSel == null ->
            merged.firstOrNull { it.lat != 0.0 && it.lng != 0.0 }
                ?: merged.firstOrNull()
        merged.none { it.orderNumber == currentSel.orderNumber } -> null
        else -> merged.firstOrNull { it.orderNumber == currentSel.orderNumber }
    }

internal fun pickDefaultSelection(
    current: OrderInfo?,
    initial: List<OrderInfo>,
): OrderInfo? =
    current ?: initial.firstOrNull { it.lat != 0.0 && it.lng != 0.0 }
        ?: initial.firstOrNull()

@Suppress("MaxLineLength")
fun MutableStateFlow<GeneralPoolUiState>.ensureSelectedStillVisible(update: GeneralPoolUiState.() -> GeneralPoolUiState) {
    this.update(update)
}

fun determineSelectionAfterDistanceUpdate(
    currentSel: OrderInfo?,
    updated: List<OrderInfo>,
    userPinned: Boolean,
): OrderInfo? {
    val nearest = updated.minByOrNull { it.distanceKm }
    val selectionHadNoDistance = currentSel?.distanceKm?.isFinite() != true
    return when {
        userPinned -> currentSel
        currentSel == null -> nearest
        selectionHadNoDistance -> nearest
        else -> currentSel
    }
}

fun updateUiWithDistances(
    updated: List<OrderInfo>,
    nextSelected: OrderInfo?,
    lastNonEmpty: (List<OrderInfo>) -> Unit,
): GeneralPoolUiState.() -> GeneralPoolUiState =
    {
        if (updated.isNotEmpty()) lastNonEmpty(updated)
        copy(orders = updated, selectedOrder = nextSelected)
    }

fun GeneralPoolUiState.removeInvalidSelectionIfNeeded(): GeneralPoolUiState {
    val sel = selectedOrder
    return if (sel != null && orders.none { it.orderNumber == sel.orderNumber }) {
        copy(selectedOrder = null)
    } else {
        this
    }
}

internal fun List<OrderInfo>.poolVisible(currentUserId: String?): List<OrderInfo> =
    filter { info ->
        val mine =
            currentUserId?.let { uid ->
                info.assignedAgentId?.equals(uid, ignoreCase = true) == true
            } ?: false
        info.status == OrderStatus.ADDED && !mine
    }
