package com.ntg.lmd.mainscreen.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.generalpool.models.OrderInfo
import com.ntg.core.location.location.domain.model.Coordinates
import com.ntg.core.location.location.domain.usecase.ComputeDistancesUseCase
import com.ntg.lmd.mainscreen.domain.paging.OrdersPaging
import com.ntg.lmd.mainscreen.domain.usecase.GetMyOrdersUseCase
import com.ntg.lmd.mainscreen.ui.model.MyOrdersPoolUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MyPoolViewModel(
    private val getMyOrders: GetMyOrdersUseCase,
    private val computeDistancesUseCase: ComputeDistancesUseCase,
) : ViewModel() {
    private val _ui = MutableStateFlow(MyOrdersPoolUiState())
    val ui: StateFlow<MyOrdersPoolUiState> = _ui.asStateFlow()

    private val deviceLocation = MutableStateFlow<Coordinates?>(null)
    val lastLocation: StateFlow<Coordinates?> = deviceLocation.asStateFlow()

    private val pageSize = OrdersPaging.PAGE_SIZE
    private var page = 1
    private var loadingJob: Job? = null

    private val pager = MyPoolPager(getMyOrders, pageSize)

    init {
        refresh()
    }

    fun updateDeviceLocation(location: Coordinates?) {
        deviceLocation.value = location
        Log.d("MyPoolVM", "Device location : $location")
        if (location != null && _ui.value.orders.isNotEmpty()) {
            val updated =
                OrderDistanceHelper.applyDistances(
                    origin = location,
                    orders = _ui.value.orders,
                    compute = computeDistancesUseCase::computeDistances,
                )
            _ui.update { it.copy(orders = updated, hasLocationPerm = true) }
            Log.d("MyPoolVM", "Device location updated: $location")
        }
    }

    fun refresh() {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch { fillFirstChunk() }
    }

    private suspend fun fillFirstChunk() {
        _ui.update {
            it.copy(isLoading = true, isLoadingMore = false, endReached = false, orders = emptyList())
        }

        val acc = ArrayList<OrderInfo>(pageSize)
        val result =
            runCatching {
                fillPagesForInitial(
                    pageSize = pageSize,
                    fetch = { p, l -> getMyOrders(page = p, limit = l, userOrdersOnly = true) },
                    acc = acc,
                )
            }

        val (reachedEnd, lastPage) =
            result.getOrElse { e ->
                _ui.update { it.copy(isLoading = false) }
                return
            }

        page = lastPage.coerceAtLeast(1)

        val updated =
            OrderDistanceHelper.applyDistances(
                origin = deviceLocation.value,
                orders = acc,
                compute = computeDistancesUseCase::computeDistances,
            )

        _ui.update {
            it.copy(
                isLoading = false,
                orders = updated,
                isLoadingMore = false,
                endReached = reachedEnd && acc.isEmpty(),
            )
        }
    }

    fun loadNextIfNeeded(currentIndex: Int) {
        val state = _ui.value
        if (state.isLoading || state.isLoadingMore || state.endReached) return
        if (currentIndex < state.orders.size - NEAR_END_THRESHOLD) return

        loadingJob?.cancel()
        loadingJob = viewModelScope.launch { doPagedAppend() }
    }

    private suspend fun doPagedAppend() {
        _ui.update { it.copy(isLoadingMore = true) }

        val result =
            pager.loadUntilAppend(
                startPage = page + 1,
                onAppend = ::applyAppend,
            )

        when (result) {
            is MyPoolPager.LoadResult.Appended -> {
                page = result.pageAt
                _ui.update { it.copy(isLoadingMore = false) }
            }

            is MyPoolPager.LoadResult.EndReached -> {
                page = result.pageAt
                _ui.update { it.copy(isLoadingMore = false, endReached = true) }
            }

            is MyPoolPager.LoadResult.NoChange -> {
                page = result.pageAt
                _ui.update { it.copy(isLoadingMore = false, endReached = false) }
            }

            is MyPoolPager.LoadResult.Error -> {
                _ui.update { it.copy(isLoadingMore = false) }
                Log.e("MyPoolVM", "Paging load failed: ${result.throwable.message}", result.throwable)
            }
        }
    }

    private fun applyAppend(
        items: List<OrderInfo>,
        rawCount: Int,
        curPage: Int,
    ) {
        val merged = mergeById(_ui.value.orders, items)
        val updated =
            OrderDistanceHelper.applyDistances(
                origin = deviceLocation.value,
                orders = merged,
                compute = computeDistancesUseCase::computeDistances,
            )

        page = curPage
        _ui.update {
            it.copy(
                orders = updated,
                isLoadingMore = false,
                endReached = rawCount < pageSize,
            )
        }
    }

    fun onCenteredOrderChange(
        order: OrderInfo,
        index: Int = 0,
    ) {
        _ui.update { it.copy(selectedOrderNumber = order.orderNumber) }
        loadNextIfNeeded(index)
    }

    override fun onCleared() {
        loadingJob?.cancel(CancellationException("ViewModel cleared"))
        super.onCleared()
    }

    companion object {
        private const val NEAR_END_THRESHOLD = 2
    }
}
