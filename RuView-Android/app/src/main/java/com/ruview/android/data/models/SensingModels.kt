package com.ruview.android.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── WebSocket Frame ────────────────────────────────────────────────────────

@Serializable
data class SensingFrame(
    val timestamp: Long = 0L,
    val persons: List<Person> = emptyList(),
    @SerialName("presence_detected") val presenceDetected: Boolean = false,
    @SerialName("sensor_count") val sensorCount: Int = 0,
    @SerialName("signal_strength") val signalStrength: Float = 0f,
    @SerialName("frame_rate") val frameRate: Float = 0f,
    @SerialName("_simulated") val simulated: Boolean = false
)

@Serializable
data class Person(
    val id: Int = 0,
    val keypoints: List<Keypoint> = emptyList(),
    @SerialName("bounding_box") val boundingBox: BoundingBox? = null,
    val confidence: Float = 0f,
    @SerialName("breathing_rate") val breathingRate: Float? = null,
    @SerialName("heart_rate") val heartRate: Float? = null
)

@Serializable
data class Keypoint(
    val x: Float,
    val y: Float,
    val confidence: Float,
    val name: String = ""
)

@Serializable
data class BoundingBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

// ─── Vital Signs ─────────────────────────────────────────────────────────────

@Serializable
data class VitalSigns(
    @SerialName("breathing_rate") val breathingRate: Float = 0f,
    @SerialName("breathing_confidence") val breathingConfidence: Float = 0f,
    @SerialName("heart_rate") val heartRate: Float = 0f,
    @SerialName("heart_rate_confidence") val heartRateConfidence: Float = 0f,
    @SerialName("person_count") val personCount: Int = 0,
    val timestamp: Long = 0L
)

// ─── Zones ────────────────────────────────────────────────────────────────────

@Serializable
data class Zone(
    val id: String,
    val name: String,
    val occupied: Boolean = false,
    @SerialName("person_count") val personCount: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 1f,
    val height: Float = 1f,
    val confidence: Float = 0f
)

@Serializable
data class ZonesResponse(
    val zones: List<Zone> = emptyList(),
    val timestamp: Long = 0L
)

// ─── MAT (Mass Casualty Triage) ───────────────────────────────────────────────

@Serializable
data class MatResponse(
    @SerialName("total_detected") val totalDetected: Int = 0,
    @SerialName("immediate_count") val immediateCount: Int = 0,   // Red
    @SerialName("delayed_count") val delayedCount: Int = 0,       // Yellow
    @SerialName("minimal_count") val minimalCount: Int = 0,       // Green
    @SerialName("expectant_count") val expectantCount: Int = 0,   // Black
    val alerts: List<MatAlert> = emptyList(),
    val zones: List<Zone> = emptyList(),
    val timestamp: Long = 0L
)

@Serializable
data class MatAlert(
    val id: String,
    val zone: String,
    val priority: AlertPriority,
    val message: String,
    val timestamp: Long = 0L
)

enum class AlertPriority { IMMEDIATE, DELAYED, MINIMAL, EXPECTANT }

// ─── Server Status ────────────────────────────────────────────────────────────

@Serializable
data class ServerStatus(
    val status: String = "unknown",
    val version: String = "",
    @SerialName("uptime_seconds") val uptimeSeconds: Long = 0L,
    @SerialName("sensor_mode") val sensorMode: String = "simulated"
)

// ─── Connection States ────────────────────────────────────────────────────────

enum class ConnectionStatus {
    CONNECTING, CONNECTED, RECONNECTING, DISCONNECTED, SIMULATED;

    val label: String get() = when (this) {
        CONNECTING    -> "Connecting..."
        CONNECTED     -> "LIVE - ESP32"
        RECONNECTING  -> "Reconnecting..."
        DISCONNECTED  -> "Disconnected"
        SIMULATED     -> "Simulated Data"
    }
}

// ─── COCO 17 keypoint skeleton connections ────────────────────────────────────

val COCO_SKELETON_CONNECTIONS = listOf(
    // Head
    Pair(0, 1), Pair(0, 2),
    Pair(1, 3), Pair(2, 4),
    // Torso
    Pair(5, 6),
    Pair(5, 11), Pair(6, 12),
    Pair(11, 12),
    // Arms
    Pair(5, 7), Pair(7, 9),
    Pair(6, 8), Pair(8, 10),
    // Legs
    Pair(11, 13), Pair(13, 15),
    Pair(12, 14), Pair(14, 16)
)

val COCO_KEYPOINT_NAMES = listOf(
    "nose", "left_eye", "right_eye", "left_ear", "right_ear",
    "left_shoulder", "right_shoulder", "left_elbow", "right_elbow",
    "left_wrist", "right_wrist", "left_hip", "right_hip",
    "left_knee", "right_knee", "left_ankle", "right_ankle"
)
