package com.ntg.lmd.mainscreen.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntg.core.location.location.domain.model.Coordinates
import com.ntg.core.location.location.domain.usecase.ComputeDistancesUseCase
import com.ntg.lmd.mainscreen.domain.usecase.GetMyOrdersUseCase
import kotlinx.coroutines.flow.update

class OrdersListViewModel(
    private val store: OrdersStore,
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
) : ViewModel() {
    private val helpers = OrdersListHelpers(store, computeDistancesUseCase)

    private val pager = OrdersPager(getMyOrders)
    private val publisher = OrdersListPublisher(store, helpers)
    private val errors = OrdersListErrorHandler(store, helpers)
    private val throttle = OrdersThrottle()

    private val deps =
        OrdersListControllerDeps(
            store = store,
            pager = pager,
            publisher = publisher,
            errors = errors,
            throttle = throttle,
        )

    private val controller =
        OrdersListController(
            deps = deps,
            scope = viewModelScope,
        )

    fun updateDeviceLocation(coords: Coordinates?) {
        store.deviceLocation.value = coords
        if (coords != null &&
            store.state.value.orders
                .isNotEmpty()
        ) {
            val computed = helpers.withDistances(coords, store.state.value.orders)
            store.state.update { it.copy(orders = computed) }
        }
    }

    fun setCurrentUserId(id: String?) = controller.setCurrentUserId(id)

    fun loadOrders(context: Context) = controller.loadInitial(context)

    fun retry(context: Context) = controller.loadInitial(context)

    fun refresh(context: Context) = controller.refresh(context)

    fun refreshOrders() = controller.refreshStrict()

    fun loadNextPage(context: Context) = controller.loadNextPage(context)
}
