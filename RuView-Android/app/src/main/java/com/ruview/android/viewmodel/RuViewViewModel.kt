package com.ruview.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruview.android.data.models.MatResponse
import com.ruview.android.data.models.SensingFrame
import com.ruview.android.data.models.VitalSigns
import com.ruview.android.data.models.Zone
import com.ruview.android.data.models.ZonesResponse
import com.ruview.android.data.repository.RuViewRepository
import com.ruview.android.data.websocket.WsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RuViewViewModel @Inject constructor(
    private val repository: RuViewRepository
) : ViewModel() {

    // ─── Server URL ───────────────────────────────────────────────────────────

    private val _serverUrl = MutableStateFlow("http://192.168.1.100:3000")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    // ─── WebSocket ────────────────────────────────────────────────────────────

    val wsState: StateFlow<WsState> = repository.wsState
        .stateIn(viewModelScope, SharingStarted.Eagerly, WsState.DISCONNECTED)

    val sensingFrame: StateFlow<SensingFrame> = repository.sensingFrame
        .stateIn(viewModelScope, SharingStarted.Eagerly, SensingFrame())

    // ─── REST Data ────────────────────────────────────────────────────────────

    private val _vitalSigns = MutableStateFlow(VitalSigns())
    val vitalSigns: StateFlow<VitalSigns> = _vitalSigns.asStateFlow()

    private val _zones = MutableStateFlow<List<Zone>>(emptyList())
    val zones: StateFlow<List<Zone>> = _zones.asStateFlow()

    private val _matData = MutableStateFlow(MatResponse())
    val matData: StateFlow<MatResponse> = _matData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ─── Settings ─────────────────────────────────────────────────────────────

    private val _showSignalOverlay = MutableStateFlow(true)
    val showSignalOverlay: StateFlow<Boolean> = _showSignalOverlay.asStateFlow()

    private val _alertsEnabled = MutableStateFlow(true)
    val alertsEnabled: StateFlow<Boolean> = _alertsEnabled.asStateFlow()

    // ─── Init ─────────────────────────────────────────────────────────────────

    init {
        connectWebSocket()
    }

    fun connectWebSocket() {
        repository.connectWebSocket(_serverUrl.value)
    }

    fun disconnect() {
        repository.disconnectWebSocket()
    }

    fun updateServerUrl(url: String) {
        _serverUrl.value = url
        repository.disconnectWebSocket()
        repository.connectWebSocket(url)
    }

    fun refreshVitalSigns() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getVitalSigns()
                .onSuccess { _vitalSigns.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun refreshZones() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getZones()
                .onSuccess { _zones.value = it.zones }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun refreshMat() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getMat()
                .onSuccess { _matData.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun toggleSignalOverlay() {
        _showSignalOverlay.value = !_showSignalOverlay.value
    }

    fun toggleAlerts() {
        _alertsEnabled.value = !_alertsEnabled.value
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnectWebSocket()
    }
}
