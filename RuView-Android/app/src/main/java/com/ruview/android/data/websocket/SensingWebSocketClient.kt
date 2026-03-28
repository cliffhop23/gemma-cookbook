package com.ruview.android.data.websocket

import com.ruview.android.data.models.Keypoint
import com.ruview.android.data.models.Person
import com.ruview.android.data.models.SensingFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class WsState { CONNECTING, CONNECTED, RECONNECTING, DISCONNECTED, SIMULATED }

@Singleton
class SensingWebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _frame = MutableStateFlow(SensingFrame())
    val frame: StateFlow<SensingFrame> = _frame.asStateFlow()

    private val _state = MutableStateFlow(WsState.DISCONNECTED)
    val state: StateFlow<WsState> = _state.asStateFlow()

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var simulationJob: Job? = null
    private var retryCount = 0
    private val maxRetries = 10
    private val retryDelaysMs = longArrayOf(1000, 2000, 4000, 8000, 16000)

    fun connect(serverUrl: String) {
        disconnect()
        retryCount = 0
        doConnect(serverUrl)
    }

    private fun doConnect(serverUrl: String) {
        val wsUrl = serverUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/') + "/ws/sensing"

        _state.value = WsState.CONNECTING

        val request = Request.Builder().url(wsUrl).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Timber.d("WebSocket connected: $wsUrl")
                retryCount = 0
                _state.value = WsState.CONNECTED
                stopSimulation()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val frame = json.decodeFromString<SensingFrame>(text)
                    _frame.value = frame
                } catch (e: Exception) {
                    Timber.w("Failed to parse WebSocket message: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Timber.w("WebSocket failure: ${t.message}")
                handleReconnect(serverUrl)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Timber.d("WebSocket closed: $reason")
                if (_state.value != WsState.DISCONNECTED) {
                    handleReconnect(serverUrl)
                }
            }
        })
    }

    private fun handleReconnect(serverUrl: String) {
        if (retryCount >= maxRetries) {
            Timber.d("Max retries reached, switching to simulation")
            _state.value = WsState.SIMULATED
            startSimulation()
            return
        }

        _state.value = WsState.RECONNECTING
        val delay = retryDelaysMs[min(retryCount, retryDelaysMs.size - 1)]
        retryCount++

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(delay)
            doConnect(serverUrl)
        }
    }

    fun disconnect() {
        reconnectJob?.cancel()
        stopSimulation()
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
        _state.value = WsState.DISCONNECTED
    }

    // ─── Simulation ────────────────────────────────────────────────────────────

    private fun startSimulation() {
        stopSimulation()
        simulationJob = scope.launch {
            var tick = 0
            while (true) {
                _frame.value = generateSimulatedFrame(tick++)
                delay(50L) // 20 Hz
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun generateSimulatedFrame(tick: Int): SensingFrame {
        val t = tick * 0.05f
        val cx = 0.5f + 0.1f * sin(t * 0.3f)
        val cy = 0.5f + 0.05f * cos(t * 0.2f)

        val keypoints = listOf(
            // nose
            Keypoint(cx, cy - 0.28f, 0.95f, "nose"),
            // eyes
            Keypoint(cx - 0.04f, cy - 0.31f, 0.9f, "left_eye"),
            Keypoint(cx + 0.04f, cy - 0.31f, 0.9f, "right_eye"),
            // ears
            Keypoint(cx - 0.07f, cy - 0.28f, 0.85f, "left_ear"),
            Keypoint(cx + 0.07f, cy - 0.28f, 0.85f, "right_ear"),
            // shoulders
            Keypoint(cx - 0.13f, cy - 0.12f, 0.95f, "left_shoulder"),
            Keypoint(cx + 0.13f, cy - 0.12f, 0.95f, "right_shoulder"),
            // elbows
            Keypoint(cx - 0.18f + 0.03f * sin(t), cy + 0.05f, 0.9f, "left_elbow"),
            Keypoint(cx + 0.18f - 0.03f * sin(t), cy + 0.05f, 0.9f, "right_elbow"),
            // wrists
            Keypoint(cx - 0.16f + 0.05f * sin(t + 0.5f), cy + 0.18f, 0.85f, "left_wrist"),
            Keypoint(cx + 0.16f - 0.05f * sin(t + 0.5f), cy + 0.18f, 0.85f, "right_wrist"),
            // hips
            Keypoint(cx - 0.09f, cy + 0.12f, 0.95f, "left_hip"),
            Keypoint(cx + 0.09f, cy + 0.12f, 0.95f, "right_hip"),
            // knees
            Keypoint(cx - 0.1f + 0.02f * sin(t * 0.7f), cy + 0.28f, 0.9f, "left_knee"),
            Keypoint(cx + 0.1f - 0.02f * sin(t * 0.7f), cy + 0.28f, 0.9f, "right_knee"),
            // ankles
            Keypoint(cx - 0.09f, cy + 0.44f, 0.85f, "left_ankle"),
            Keypoint(cx + 0.09f, cy + 0.44f, 0.85f, "right_ankle")
        )

        return SensingFrame(
            timestamp = System.currentTimeMillis(),
            persons = listOf(
                Person(
                    id = 0,
                    keypoints = keypoints,
                    confidence = 0.92f,
                    breathingRate = 15f + 3f * sin(t * 0.1f),
                    heartRate = 72f + 5f * cos(t * 0.05f)
                )
            ),
            presenceDetected = true,
            sensorCount = 3,
            signalStrength = -65f + 5f * sin(t * 0.15f),
            frameRate = 20f,
            simulated = true
        )
    }
}
