package com.ntg.lmd.mainscreen.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.example.generalpool.models.OrderInfo
import com.example.generalpool.vm.UpdateOrderStatusViewModel
import com.ntg.lmd.R
import com.ntg.lmd.mainscreen.ui.model.MyOrderCardCallbacks
import org.koin.androidx.compose.koinViewModel

@Composable
fun myPoolOrderCardItem(
    order: OrderInfo,
    onOpenOrderDetails: (String) -> Unit,
    onCall: (String?) -> Unit,
) {
    Box(Modifier.width(dimensionResource(R.dimen.myOrders_card_width))) {
        val updateVm: UpdateOrderStatusViewModel = koinViewModel()
        myOrderCard(
            order = order,
            isUpdating = false,
            callbacks =
                MyOrderCardCallbacks(
                    onReassignRequested = {},
                    onDetails = { onOpenOrderDetails(order.orderNumber) },
                    onCall = { onCall(order.customerPhone) },
                    onAction = { action: OrderActions -> },
                ),
            updateVm = updateVm,
        )
    }
}
