package com.ruview.android.ui.screens.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruview.android.data.websocket.WsState
import com.ruview.android.ui.components.ConnectionBanner
import com.ruview.android.ui.components.MetricCard
import com.ruview.android.ui.components.SkeletonCanvas
import com.ruview.android.ui.theme.Background
import com.ruview.android.ui.theme.CardBackground
import com.ruview.android.ui.theme.CardBorder
import com.ruview.android.ui.theme.Primary
import com.ruview.android.ui.theme.StatusGreen
import com.ruview.android.ui.theme.StatusYellow
import com.ruview.android.ui.theme.TextSecondary
import com.ruview.android.viewmodel.RuViewViewModel

@Composable
fun LiveScreen(viewModel: RuViewViewModel = hiltViewModel()) {
    val frame by viewModel.sensingFrame.collectAsState()
    val wsState by viewModel.wsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with connection banner
        ConnectionBanner(
            wsState = wsState,
            isSimulated = frame.simulated
        )

        // Skeleton visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
        ) {
            SkeletonCanvas(
                persons = frame.persons,
                modifier = Modifier.fillMaxSize()
            )

            // HUD Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HudLabel("FPS", String.format("%.0f", frame.frameRate))
                HudLabel("RSSI", String.format("%.0f dBm", frame.signalStrength))
                HudLabel("Sensors", "${frame.sensorCount}")
            }

            // Person count badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(
                        if (frame.presenceDetected) StatusGreen.copy(alpha = 0.2f)
                        else CardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${frame.persons.size} person${if (frame.persons.size != 1) "s" else ""}",
                    color = if (frame.presenceDetected) StatusGreen else TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Metrics row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val firstPerson = frame.persons.firstOrNull()

            MetricCard(
                label = "Heart Rate",
                value = firstPerson?.heartRate?.let { String.format("%.0f", it) } ?: "--",
                unit = "BPM",
                accentColor = StatusYellow,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Breathing",
                value = firstPerson?.breathingRate?.let { String.format("%.0f", it) } ?: "--",
                unit = "BPM",
                accentColor = Primary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Persons",
                value = "${frame.persons.size}",
                unit = "detected",
                accentColor = StatusGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode badge
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .background(
                    color = when (wsState) {
                        WsState.CONNECTED -> StatusGreen.copy(alpha = 0.1f)
                        WsState.SIMULATED -> Primary.copy(alpha = 0.1f)
                        else -> CardBorder.copy(alpha = 0.5f)
                    },
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = when {
                    wsState == WsState.CONNECTED && !frame.simulated ->
                        "Signal-Derived · ${frame.sensorCount} sensor${if (frame.sensorCount != 1) "s" else ""}"
                    wsState == WsState.SIMULATED || frame.simulated ->
                        "Model Inference · Simulated"
                    else -> "Connecting..."
                },
                color = when (wsState) {
                    WsState.CONNECTED -> StatusGreen
                    WsState.SIMULATED -> Primary
                    else -> TextSecondary
                },
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HudLabel(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = Primary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
