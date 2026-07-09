package com.example.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

class SyncManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("offline_sync_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null

    // Connected to Render Production Environment
    private val wsClient = KurotekWebSocketClient("wss://kayan-uzs5.onrender.com/ws") { message ->
        handleIncomingMessage(message)
    }

    init {
        // Observe connection state to trigger sync when connected
        scope.launch {
            wsClient.connectionState.collect { state ->
                if (state == ConnectionState.CONNECTED) {
                    processOfflineQueue()
                }
            }
        }
    }

    fun startSync() {
        Log.d(TAG, "Starting sync manager")
        wsClient.connect()
    }

    fun stopSync() {
        Log.d(TAG, "Stopping sync manager")
        syncJob?.cancel()
        wsClient.disconnect()
    }

    /**
     * Enqueues a message. If connected, it tries to send immediately.
     * If sending fails or disconnected, it saves to offline queue.
     */
    fun enqueueMessage(payload: JSONObject) {
        val messageStr = payload.toString()
        if (wsClient.connectionState.value == ConnectionState.CONNECTED) {
            val sent = wsClient.sendMessage(messageStr)
            if (!sent) {
                saveToOfflineQueue(messageStr)
            }
        } else {
            saveToOfflineQueue(messageStr)
        }
    }

    @Synchronized
    private fun saveToOfflineQueue(message: String) {
        val queueStr = prefs.getString(PREF_QUEUE, "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(queueStr)
            jsonArray.put(message)
            prefs.edit().putString(PREF_QUEUE, jsonArray.toString()).apply()
            Log.d(TAG, "Message saved to offline queue. Total: ${jsonArray.length()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to offline queue", e)
        }
    }

    @Synchronized
    private fun processOfflineQueue() {
        if (syncJob?.isActive == true) return

        syncJob = scope.launch {
            val queueStr = prefs.getString(PREF_QUEUE, "[]") ?: "[]"
            try {
                val jsonArray = JSONArray(queueStr)
                if (jsonArray.length() == 0) return@launch

                Log.d(TAG, "Processing offline queue with ${jsonArray.length()} items")
                val newArray = JSONArray()

                for (i in 0 until jsonArray.length()) {
                    val message = jsonArray.getString(i)
                    if (wsClient.connectionState.value == ConnectionState.CONNECTED) {
                        val sent = wsClient.sendMessage(message)
                        if (!sent) {
                            newArray.put(message) // Keep in queue if failed
                        }
                    } else {
                        newArray.put(message) // Keep in queue if disconnected during process
                    }
                    delay(100) // Small delay between messages to prevent flooding
                }

                prefs.edit().putString(PREF_QUEUE, newArray.toString()).apply()
                if (newArray.length() > 0) {
                    Log.d(TAG, "${newArray.length()} items remain in offline queue")
                } else {
                    Log.d(TAG, "Offline queue cleared")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing offline queue", e)
            }
        }
    }

    private fun handleIncomingMessage(message: String) {
        // Handle sync messages from server (e.g. settings updates, card allocations)
        try {
            val json = JSONObject(message)
            val action = json.optString("action")
            when (action) {
                "SYNC_SETTINGS" -> {
                    Log.d(TAG, "Received settings sync")
                    // Update local settings...
                }
                "ACK" -> {
                    // Acknowledgment for sent messages
                }
                else -> {
                    Log.d(TAG, "Unknown message action: $action")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse incoming message", e)
        }
    }

    companion object {
        private const val TAG = "SyncManager"
        private const val PREF_QUEUE = "offline_message_queue"
    }
}
