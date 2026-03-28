package com.ruview.android.ui.screens.vitals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruview.android.ui.components.ConnectionBanner
import com.ruview.android.ui.components.GaugeArc
import com.ruview.android.ui.theme.Background
import com.ruview.android.ui.theme.Primary
import com.ruview.android.ui.theme.StatusRed
import com.ruview.android.ui.theme.StatusYellow
import com.ruview.android.ui.theme.TextPrimary
import com.ruview.android.ui.theme.TextSecondary
import com.ruview.android.viewmodel.RuViewViewModel

@Composable
fun VitalsScreen(viewModel: RuViewViewModel = hiltViewModel()) {
    val frame by viewModel.sensingFrame.collectAsState()
    val wsState by viewModel.wsState.collectAsState()

    val firstPerson = frame.persons.firstOrNull()
    val breathingRate = firstPerson?.breathingRate ?: 0f
    val heartRate = firstPerson?.heartRate ?: 0f
    val confidence = firstPerson?.confidence ?: 0f

    // Keep last 60 samples for sparklines
    val breathingHistory = remember { mutableStateListOf<Float>() }
    val heartRateHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(frame.timestamp) {
        if (breathingRate > 0f) {
            breathingHistory.add(breathingRate)
            if (breathingHistory.size > 60) breathingHistory.removeAt(0)
        }
        if (heartRate > 0f) {
            heartRateHistory.add(heartRate)
            if (heartRateHistory.size > 60) heartRateHistory.removeAt(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        ConnectionBanner(wsState = wsState, isSimulated = frame.simulated)

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "VITAL SIGNS",
            color = Primary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Person status
        Text(
            text = if (frame.presenceDetected) "Person detected · Confidence ${String.format("%.0f%%", confidence * 100)}"
            else "No person detected",
            color = if (frame.presenceDetected) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Gauge row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GaugeArc(
                value = breathingRate,
                minValue = 0f,
                maxValue = 40f,
                label = "BREATHING",
                unit = "BPM",
                accentColor = Primary,
                size = 160.dp
            )
            GaugeArc(
                value = heartRate,
                minValue = 0f,
                maxValue = 150f,
                label = "HEART RATE",
                unit = "BPM",
                accentColor = StatusRed,
                size = 160.dp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Range indicators
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VitalRangeRow(
                label = "Breathing Rate",
                value = breathingRate,
                unit = "BPM",
                normalMin = 6f,
                normalMax = 30f,
                accentColor = Primary
            )
            VitalRangeRow(
                label = "Heart Rate",
                value = heartRate,
                unit = "BPM",
                normalMin = 40f,
                normalMax = 120f,
                accentColor = StatusRed
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sparklines
        if (breathingHistory.isNotEmpty()) {
            Text(
                text = "HISTORY (last 60 frames)",
                color = TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SparklineChart(
                data = breathingHistory,
                color = Primary,
                label = "Breathing",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SparklineChart(
                data = heartRateHistory,
                color = StatusRed,
                label = "Heart Rate",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun VitalRangeRow(
    label: String,
    value: Float,
    unit: String,
    normalMin: Float,
    normalMax: Float,
    accentColor: androidx.compose.ui.graphics.Color
) {
    val isNormal = value in normalMin..normalMax
    val status = when {
        value == 0f -> "No Signal"
        isNormal -> "Normal"
        value < normalMin -> "Low"
        else -> "High"
    }
    val statusColor = when {
        value == 0f -> TextSecondary
        isNormal -> accentColor
        else -> StatusYellow
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text(
                text = "Normal: $normalMin–$normalMax $unit",
                color = TextSecondary.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (value > 0f) "${String.format("%.1f", value)} $unit" else "--",
                color = accentColor,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(text = status, color = statusColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}
