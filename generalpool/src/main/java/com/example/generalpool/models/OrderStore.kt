package com.example.generalpool.models

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OrderStore {
    private val _state = MutableStateFlow<List<Order>>(emptyList())
    val state: StateFlow<List<Order>> = _state

    private val ch = Channel<Order>(Channel.UNLIMITED)
    fun channel(): Channel<Order> = ch

    fun add(o: Order) {
        val list = _state.value.toMutableList()
        if (list.none { it.id == o.id }) {
            list.add(0, o)
            _state.value = list
        }
        ch.trySend(o)
    }

    fun update(o: Order) {
        val list = _state.value.toMutableList()
        val i = list.indexOfFirst { it.id == o.id }
        if (i != -1) {
            list[i] = o
            _state.value = list
        }
        ch.trySend(o)
    }

    fun remove(id: String) {
        val list = _state.value.toMutableList()
        if (list.removeAll { it.id == id }) _state.value = list
    }
}
