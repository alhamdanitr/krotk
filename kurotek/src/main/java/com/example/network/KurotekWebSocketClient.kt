package com.example.network

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

class KurotekWebSocketClient(
    private val url: String,
    private val onMessageReceived: (String) -> Unit
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var isClosedManually = false

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // Coroutine scope for reconnect attempts
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var reconnectJob: Job? = null
    private var reconnectDelay = 1000L

    fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTED || _connectionState.value == ConnectionState.CONNECTING) return

        isClosedManually = false
        _connectionState.value = ConnectionState.CONNECTING
        Log.d(TAG, "Connecting to $url...")

        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                _connectionState.value = ConnectionState.CONNECTED
                reconnectDelay = 1000L // Reset delay on successful connection
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                onMessageReceived(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $reason")
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure", t)
                _connectionState.value = ConnectionState.DISCONNECTED
                scheduleReconnect()
            }
        })
    }

    fun sendMessage(message: String): Boolean {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            val sent = webSocket?.send(message) == true
            if (sent) Log.d(TAG, "Message sent: $message")
            else Log.w(TAG, "Failed to send message: $message")
            return sent
        }
        return false
    }

    fun disconnect() {
        isClosedManually = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
        Log.d(TAG, "Disconnected manually")
    }

    private fun scheduleReconnect() {
        if (isClosedManually) return

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            Log.d(TAG, "Scheduling reconnect in ${reconnectDelay}ms")
            delay(reconnectDelay)
            
            // Exponential backoff up to 30 seconds
            if (reconnectDelay < 30000L) {
                reconnectDelay *= 2
            }
            
            connect()
        }
    }

    companion object {
        private const val TAG = "KurotekWebSocketClient"
    }
}
