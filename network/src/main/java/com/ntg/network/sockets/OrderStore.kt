package com.ntg.network.sockets

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Generic in-memory store decoupled from any specific model.
 * Provide how to extract the ID from an item via [idSelector].
 */
internal class EntityStore<T, ID>(
    private val idSelector: (item: T) -> ID
) {
    private val _state = MutableStateFlow<List<T>>(emptyList())
    val state: StateFlow<List<T>> get() = _state

    private val itemChannel = Channel<T>(Channel.UNLIMITED)
    fun getChannel(): Channel<T> = itemChannel

    fun add(item: T) {
        _state.update { current ->
            if (current.any { idSelector(it) == idSelector(item) }) current
            else buildList {
                add(item)
                addAll(current)
            }
        }
        itemChannel.trySend(item)
    }

    fun update(updated: T) {
        _state.update { current ->
            val targetId = idSelector(updated)
            val idx = current.indexOfFirst { idSelector(it) == targetId }
            if (idx == -1) current
            else current.toMutableList().also { it[idx] = updated }
        }
        itemChannel.trySend(updated)
    }

    fun remove(id: ID) {
        _state.update { current ->
            current.filterNot { idSelector(it) == id }
        }
    }
}
