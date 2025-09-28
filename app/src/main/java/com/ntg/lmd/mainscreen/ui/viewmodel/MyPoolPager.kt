package com.ntg.lmd.mainscreen.ui.viewmodel

import com.example.generalpool.models.OrderInfo
import com.ntg.lmd.mainscreen.domain.usecase.GetMyOrdersUseCase

class MyPoolPager(
    private val getMyOrders: GetMyOrdersUseCase,
    private val pageSize: Int,
) {
    sealed interface LoadResult {
        data class Appended(
            val pageAt: Int,
        ) : LoadResult

        data class EndReached(
            val pageAt: Int,
        ) : LoadResult

        data class NoChange(
            val pageAt: Int,
        ) : LoadResult

        data class Error(
            val throwable: Throwable,
        ) : LoadResult
    }

    suspend fun loadUntilAppend(
        startPage: Int,
        onAppend: (List<OrderInfo>, Int, Int) -> Unit,
    ): LoadResult {
        var pageAt = startPage
        var hops = 0
        var outcome: LoadResult = LoadResult.NoChange(pageAt)
        var done = false

        while (!done && hops < PREFETCH_AHEAD_PAGES) {
            val attempt =
                runCatching {
                    getMyOrders(page = pageAt, limit = pageSize, userOrdersOnly = true)
                }

            outcome =
                attempt.fold(
                    onSuccess = { result ->
                        val items = result as List<OrderInfo>
                        val reachedEnd = items.size < pageSize
                        if (items.isEmpty()) {
                            LoadResult.EndReached(pageAt)
                        } else {
                            onAppend(items, items.size, pageAt)
                            if (reachedEnd) LoadResult.EndReached(pageAt) else LoadResult.Appended(pageAt)
                        }
                    },
                    onFailure = { LoadResult.Error(it) },
                )

            done = outcome !is LoadResult.NoChange
            if (outcome is LoadResult.NoChange) {
                pageAt++
                hops++
            }
        }

        return outcome
    }

    companion object {
        private const val PREFETCH_AHEAD_PAGES = 3
    }
}
