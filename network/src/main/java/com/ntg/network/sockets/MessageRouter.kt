package com.ntg.network.sockets

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableSharedFlow


/**
 * Domain-agnostic handler for DB change events.
 * Implement in your feature module (e.g., map JsonObject -> Order and update your store).
 */
interface ChangeHandler {
    fun onInsert(record: JsonObject)
    fun onUpdate(record: JsonObject)
    fun onDelete(id: String?, oldRecord: JsonObject?)
}

/**
 * MessageRouter no longer depends on Order / OrderStore.
 * It parses incoming messages and delegates to ChangeHandler.
 */
internal class MessageRouter(
    private val handler: ChangeHandler,
    private val events: MutableSharedFlow<SocketEvent>,
    private val logTag: String,
) {
    fun route(text: String) {
        Log.d(logTag, "RAW -> $text")
        try {
            val root = JsonParser.parseString(text).asJsonObject
            val event = root.get("event")?.asString.orEmpty()

            when (event) {
                "phx_reply" -> Log.d(logTag, "Channel joined")
                "INSERT", "UPDATE", "DELETE" -> handleClassic(root, event)
                "postgres_changes" -> handlePostgresChange(root)
                "presence_state", "presence_diff", "system", "ping", "phx_close" -> Unit
                else -> Unit
            }

            events.tryEmit(SocketEvent.Message(text))
        } catch (e: JsonSyntaxException) {
            Log.e(logTag, "JSON syntax error: ${e.message}. Raw=$text", e)
            events.tryEmit(SocketEvent.Error(e))
        } catch (e: JsonParseException) {
            Log.e(logTag, "JSON parse error: ${e.message}. Raw=$text", e)
            events.tryEmit(SocketEvent.Error(e))
        } catch (e: IllegalStateException) {
            Log.e(logTag, "Illegal JSON state: ${e.message}. Raw=$text", e)
            events.tryEmit(SocketEvent.Error(e))
        }
    }

    private fun handleClassic(root: JsonObject, event: String) {
        val payload = root.getAsJsonObject("payload") ?: JsonObject()
        val record = payload.getAsJsonObject("record")
        val oldRecord = payload.getAsJsonObject("old_record")

        when (event) {
            "INSERT" -> {
                if (record != null) {
                    Log.d(logTag, "INSERT -> $record")
                    handler.onInsert(record)
                }
            }
            "UPDATE" -> {
                if (record != null) {
                    Log.d(logTag, "UPDATE -> $record")
                    handler.onUpdate(record)
                }
            }
            "DELETE" -> {
                val id = oldRecord?.get("id")?.asString
                Log.d(logTag, "DELETE -> id=$id")
                handler.onDelete(id, oldRecord)
            }
        }
    }

    private fun handlePostgresChange(root: JsonObject) {
        val payload = root.getAsJsonObject("payload")
        val data = payload?.getAsJsonObject("data")
        val type = data?.get("eventType")?.asString

        when (type) {
            "INSERT" -> {
                val newJson = data?.getAsJsonObject("new")
                if (newJson != null) {
                    Log.d(logTag, "INSERT -> $newJson")
                    handler.onInsert(newJson)
                }
            }
            "UPDATE" -> {
                val newJson = data?.getAsJsonObject("new")
                if (newJson != null) {
                    Log.d(logTag, "UPDATE -> $newJson")
                    handler.onUpdate(newJson)
                }
            }
            "DELETE" -> {
                val oldJson = data?.getAsJsonObject("old")
                val id = oldJson?.get("id")?.asString
                Log.d(logTag, "DELETE -> id=$id")
                handler.onDelete(id, oldJson)
            }
        }
    }
}
