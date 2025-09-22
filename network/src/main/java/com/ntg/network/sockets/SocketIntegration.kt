package com.ntg.network.sockets

import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import com.ntg.network.authheader.TokenStore

data class WebSocketMessage(
    @SerializedName("type")
    val type: String?,
    @SerializedName("data")
    val data: Any?,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
)

private const val WS_CLOSE_NORMAL = 1000
private const val HTTP_UNAUTHORIZED = 401
private const val DEFAULT_RETRY_DELAY_MS = 3000L
private const val HEARTBEAT_INTERVAL_MS = 28_000L // ~28s (before Phoenix’s 30s timeout)

/**
 * Domain-agnostic socket integration:
 * - Uses TokenStore (not SecureTokenStore)
 * - Delegates data changes to a ChangeHandler through MessageRouter
 * - Emits connection + raw message events via [events]
 */
class SocketIntegration(
    private val baseWsUrl: String,
    private val client: OkHttpClient,
    private val tokenStore: TokenStore,
    private val handler: ChangeHandler,
    tag: String = "LMD-WS",
) {

    private val logTag = tag
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<SocketEvent> = _events

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var hbJob: Job? = null

    @Volatile
    private var refCounter = 1

    private val gson = Gson()

    private val recon =
        ReconnectController(Looper.getMainLooper()) {
            currentChannelName?.let { connect(it) }
        }

    private val router = MessageRouter(handler, _events, logTag)

    private var ws: WebSocket? = null
    private var lastAccess: String? = null
    private var currentChannelName: String? = null
    private var isDestroyed = false

    @Volatile
    private var listenerStarted = false

    private val connector =
        ConnectionController(
            baseWsUrl = baseWsUrl,
            client = client,
            tokenStore = tokenStore,
            listener =
                object : ConnectionListener {
                    override fun onOpen() {
                        _connectionState.value = ConnectionState.CONNECTED
                        startHeartbeat()
                    }

                    override fun onClosed(code: Int, reason: String) {
                        stopHeartbeat()
                        _connectionState.value = ConnectionState.DISCONNECTED
                        if (code != WS_CLOSE_NORMAL) recon.schedule(DEFAULT_RETRY_DELAY_MS)
                    }

                    override fun onFailure(httpCode: Int?, message: String?, t: Throwable?) {
                        stopHeartbeat()
                        if (httpCode == HTTP_UNAUTHORIZED) {
                            _connectionState.value =
                                ConnectionState.ERROR("Authentication failed - token refresh needed")
                            recon.schedule(DEFAULT_RETRY_DELAY_MS)
                        } else {
                            _connectionState.value =
                                ConnectionState.ERROR(message ?: t?.message ?: "Connection failed")
                            recon.schedule(DEFAULT_RETRY_DELAY_MS)
                        }
                    }

                    override fun onMessage(text: String) {
                        router.route(text)
                    }
                },
            logTag = logTag,
        )

    fun connect(channelName: String) {
        if (isDestroyed) {
            Log.w(logTag, "connect() called after destroy")
            return
        }

        val state = _connectionState.value
        val alreadyConnected =
            currentChannelName == channelName &&
                    (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING)

        when {
            alreadyConnected -> {
                Log.d(logTag, "connect() ignored: already $state to $channelName")
            }

            tokenStore.getAccessToken().isNullOrEmpty() -> {
                _connectionState.value = ConnectionState.ERROR("No authentication token")
            }

            else -> {
                ws?.close(WS_CLOSE_NORMAL, "Reconnecting to $channelName")
                ws = null

                lastAccess = tokenStore.getAccessToken()
                currentChannelName = channelName
                _connectionState.value = ConnectionState.CONNECTING
                recon.cancel()
                ws = connector.connect(channelName)
            }
        }
    }

    fun disconnect() {
        recon.cancel()
        ws?.close(WS_CLOSE_NORMAL, "User disconnected")
        ws = null
        currentChannelName = null
        listenerStarted = false
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun retryConnection() {
        if (isDestroyed) {
            Log.w(logTag, "retryConnection() after destroy")
            return
        }
        recon.cancel()
        currentChannelName?.let { connect(it) }
    }

    /**
     * Generic send helper (instead of updateOrderStatus).
     */
    fun send(event: String, payload: Any) {
        val access = tokenStore.getAccessToken()
        if (access.isNullOrEmpty()) return
        val wsMessage = WebSocketMessage(event, payload)
        ws?.send(gson.toJson(wsMessage))
    }

    fun reconnectIfTokenChanged(currentAccess: String?) {
        if (!currentAccess.isNullOrBlank() && currentAccess != lastAccess) {
            disconnect()
            connect(currentChannelName ?: "default")
        }
    }

    private fun startHeartbeat() {
        hbJob?.cancel()
        hbJob =
            scope.launch {
                while (true) {
                    val json =
                        """{"topic":"phoenix","event":"heartbeat","payload":{},"ref":"${refCounter++}"}"""
                    ws?.send(json)
                    delay(HEARTBEAT_INTERVAL_MS)
                }
            }
    }

    private fun stopHeartbeat() {
        hbJob?.cancel()
        hbJob = null
    }
}

sealed class ConnectionState {
    data object CONNECTING : ConnectionState()
    data object CONNECTED : ConnectionState()
    data object DISCONNECTED : ConnectionState()
    data class ERROR(val message: String) : ConnectionState()
}
