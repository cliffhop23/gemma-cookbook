package com.ruview.android.data.repository

import com.ruview.android.data.api.RuViewApiService
import com.ruview.android.data.models.MatResponse
import com.ruview.android.data.models.SensingFrame
import com.ruview.android.data.models.ServerStatus
import com.ruview.android.data.models.VitalSigns
import com.ruview.android.data.models.Zone
import com.ruview.android.data.models.ZonesResponse
import com.ruview.android.data.websocket.SensingWebSocketClient
import com.ruview.android.data.websocket.WsState
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuViewRepository @Inject constructor(
    private val apiService: RuViewApiService,
    private val wsClient: SensingWebSocketClient
) {
    val sensingFrame: StateFlow<SensingFrame> = wsClient.frame
    val wsState: StateFlow<WsState> = wsClient.state

    fun connectWebSocket(serverUrl: String) {
        wsClient.connect(serverUrl)
    }

    fun disconnectWebSocket() {
        wsClient.disconnect()
    }

    suspend fun getServerStatus(): Result<ServerStatus> = runCatching {
        apiService.getHealth()
    }.onFailure { Timber.e(it, "Failed to get server status") }

    suspend fun getVitalSigns(): Result<VitalSigns> = runCatching {
        apiService.getVitalSigns()
    }.onFailure { Timber.e(it, "Failed to get vital signs") }

    suspend fun getZones(): Result<ZonesResponse> = runCatching {
        apiService.getZones()
    }.onFailure { Timber.e(it, "Failed to get zones") }

    suspend fun getMat(): Result<MatResponse> = runCatching {
        apiService.getMat()
    }.onFailure { Timber.e(it, "Failed to get MAT data") }
}
