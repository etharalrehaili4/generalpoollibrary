package com.ntg.lmd.mainscreen.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.auth.utils.SecureUserStore
import com.ntg.core.location.location.domain.model.Coordinates
import com.ntg.core.location.location.domain.usecase.ComputeDistancesUseCase
import com.ntg.lmd.mainscreen.domain.usecase.GetMyOrdersUseCase
import com.ntg.lmd.mainscreen.ui.model.MyOrdersUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MyOrdersViewModel(
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
    private val userStore: SecureUserStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyOrdersUiState(isLoading = false))
    val uiState: StateFlow<MyOrdersUiState> = _uiState.asStateFlow()

    private val currentUserId = MutableStateFlow<String?>(userStore.toString())
    private val deviceLocation = MutableStateFlow<Coordinates?>(null)

    // Shared store across sub-ViewModels
    private val store =
        OrdersStore(
            state = _uiState,
            currentUserId = currentUserId,
            deviceLocation = deviceLocation,
            allOrders = mutableListOf(),
        )

    val listVM = OrdersListViewModel(store, getMyOrders, computeDistancesUseCase)
    val searchVM = OrdersSearchViewModel(store)
    val statusVM = OrdersStatusViewModel(store)

    init {
        listVM.refreshOrders()
    }
}
